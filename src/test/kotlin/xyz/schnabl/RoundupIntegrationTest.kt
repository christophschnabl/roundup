package xyz.schnabl

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.schnabl.remote.AmountDto
import xyz.schnabl.remote.GsonSerdeService
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.feed.FeedItemDto
import xyz.schnabl.remote.feed.TransactionDirection
import xyz.schnabl.remote.feed.TransactionFeedDto
import xyz.schnabl.remote.savings.CreateSavingsGoalDto
import xyz.schnabl.remote.savings.CreateSavingsTransferDto
import xyz.schnabl.remote.savings.SavingsGoalDto
import xyz.schnabl.remote.savings.TransferSavingsGoalDto
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals


class RoundupIntegrationTest {
    private val server = MockWebServer()

    private val GBP = Currency.getInstance("GBP")
    private val accountUid = UUID.fromString("a7ab343e-ef3e-4e39-a850-2d0c5bb44cde")
    private val defaultCategory = UUID.fromString("0791a3a1-487c-4998-b11b-3852dc833ed3")
    private val savingGoalsUid = UUID.fromString("303f02e2-90af-4444-a0a1-cec0469b9207")
    private val expectedAccount = AccountDto(accountUid, defaultCategory, GBP, parseTime("2020-03-22T19:33:48.990Z"))

    private val gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        JsonDeserializer { json: JsonElement, _, _ ->
            ZonedDateTime.parse(
                json.asJsonPrimitive.asString
            ).toLocalDateTime()
        } as JsonDeserializer<LocalDateTime>
    ).create()

    private lateinit var client : StarlingClient

    @Before
    fun setUp() {
        server.start()
        // TODO load config dynamically
        val config = Config("http://${server.hostName}:${server.port}/api/v2", "accounts", "feed", "category", "savings-goals", "account", "add-money")
        client = StarlingClient(OkHttpClient(), config, GsonSerdeService(gson))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `testGETAccountsForUserHappyPath`() {
        val expected = listOf(expectedAccount)
        val expectedUrl = server.url("api/v2/accounts")

        // Given
        server.enqueue(responseBodyFromJson("account.json"))

        // When
        val actual = client.getAccountsForUser()

        val recordedRequest = server.takeRequest()

        // Then
        assertEquals(expected, actual)
        assertEquals(expectedUrl, recordedRequest.requestUrl)
    }


    @Test
    fun `testGETFeedHappyPath`() {
        val transactionTime1 = parseTime("2020-03-26T20:44:45.601Z")
        val settlementTime1 = parseTime("2020-03-26T20:44:45.839Z")

        val feedItem1Uid = UUID.fromString("26bc1249-dc3e-481e-bf81-b2872f224f28")
        val feedItem1Amount = AmountDto(GBP, 4242)
        val feedItem1 = FeedItemDto(feedItem1Uid, defaultCategory, feedItem1Amount, TransactionDirection.OUT, transactionTime1, settlementTime1)


        val transactionTime2 = parseTime("2020-03-26T20:30:04.977Z")
        val settlementTime2 = parseTime("2020-03-26T20:30:04.977Z")

        val feedItem2Uid = UUID.fromString("a6d8e32d-99db-403b-ad0e-5267f060b068")
        val feedItem2Amount = AmountDto(GBP, 10)
        val feedItem2 = FeedItemDto(feedItem2Uid, defaultCategory, feedItem2Amount, TransactionDirection.OUT, transactionTime2, settlementTime2)

        val changesSince = LocalDateTime.now()
        val expectedFeed = TransactionFeedDto(listOf(feedItem1, feedItem2))
        val expectedUrl = server.url("api/v2/feed/account/$accountUid/category/$defaultCategory?changesSince=${changesSince.toInstant(ZoneOffset.UTC)}")

        server.enqueue(responseBodyFromJson("feed.json"))

        val actualFeed = client.getTransactionsForAccountByCategory(expectedAccount.accountUid, expectedAccount.defaultCategory, changesSince)

        val recordedRequest = server.takeRequest()

        assertEquals(expectedFeed, actualFeed)
        assertEquals(expectedUrl, recordedRequest.requestUrl)
    }

    @Test
    fun `testPUTSavingsGoalHappyPath`() {
        val expectedUrl = server.url("api/v2/account/$accountUid/savings-goals")
        val expectedSavingsGoal = SavingsGoalDto(savingGoalsUid, true, listOf())
        val expectedCreateSavingsGoalDto = CreateSavingsGoalDto("Hansi", GBP, AmountDto(GBP, 4242))

        server.enqueue(responseBodyFromJson("savingsGoal.json"))

        val savingsGoal = client.createSavingsGoal(accountUid, "Hansi", 4242)

        val request = server.takeRequest()

        // CHECK RequestBody
        assertEquals(gson.toJson(expectedCreateSavingsGoalDto), request.body.toBody())

        // CHECK RequestUrl
        assertEquals(expectedUrl, request.requestUrl)

        // CHECK Response
        assertEquals(expectedSavingsGoal, savingsGoal)
    }

    @Test
    fun `testPUTTransferToSavingsGoalHappyPath`() {
        val transferUid = UUID.fromString("5d04b20b-4109-4dbe-b261-6729ba72c504")
        val expectedCreateSavingsTransfer = CreateSavingsTransferDto(AmountDto(GBP, 1337))
        val expectedSavingsTransfer = TransferSavingsGoalDto(transferUid, true, listOf())

        server.enqueue(responseBodyFromJson("transferSavingsGoal.json"))
        val savingsTransfer = client.transferToSavingsGoal(accountUid, savingGoalsUid, transferUid, 1337)

        val recordedRequest = server.takeRequest()

        // CHECK RequestBody
        assertEquals(gson.toJson(expectedCreateSavingsTransfer), recordedRequest.body.toBody())

        // CHECK Url
        val expectedUrl = server.url("api/v2/account/$accountUid/savings-goals/$savingGoalsUid/add-money/$transferUid")
        assertEquals(expectedUrl, recordedRequest.requestUrl)

        assertEquals(expectedSavingsTransfer, savingsTransfer)
    }


    private fun readJson(fileName: String): String {
        return this::class.java.getResource("/$fileName").readText()
    }

    private fun responseBodyFromJson(fileName: String): MockResponse {
        return MockResponse().setBody(readJson(fileName))
    }

    private fun requestBodyFromJson(fileName: String): RequestBody {
        return readJson(fileName).toRequestBody("application/json".toMediaType())
    }

    private fun parseTime(localDateTime: String): LocalDateTime {
        return LocalDateTime.parse(localDateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    private fun Buffer.toBody() : String {
        return String(inputStream().readAllBytes())
    }
}