package xyz.schnabl

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.schnabl.remote.GsonSerdeService
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.StarlingClientImpl
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import java.util.Currency
import kotlin.test.assertEquals


class RoundupApplicationTest {
    private val server = MockWebServer()
    private val accountUid = UUID.fromString("a7ab343e-ef3e-4e39-a850-2d0c5bb44cde")
    private val savingGoalsUid = UUID.fromString("303f02e2-90af-4444-a0a1-cec0469b9207")
    private val GBP = Currency.getInstance("GBP")


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
        val config = Config("http://${server.hostName}:${server.port}/api/v2", "accounts", "feed", "category", "savings-goals", "account", "add-money")
        client = StarlingClientImpl(OkHttpClient(), config, GsonSerdeService(gson))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `applicationTestHappyPath`() {
        val roundupService = RoundupServiceImpl(client)
        val expected = SavingsGoalInfo(savingGoalsUid, "journey", Amount(GBP, 113), Amount(GBP, 10), 11)

        // Given
        server.enqueue(responseBodyFromJson("account.json"))
        server.enqueue(responseBodyFromJson("feed.json"))
        server.enqueue(responseBodyFromJson("savingsGoal.json"))
        server.enqueue(responseBodyFromJson("transferSavingsGoal.json"))
        server.enqueue(responseBodyFromJson("savingsGoalInfo.json"))

        val transactions = roundupService.getAllOutgoingTransactionsForFirstAccount()
        val roundup = roundupService.getRoundupSumForTransactions(transactions.second)

        val savingsGoalInfo = roundupService.createAndTransferToSavingsGoal(accountUid, "journey", 113, roundup)

        assertEquals(expected, savingsGoalInfo)
    }


    private fun readJson(fileName: String): String {
        return this::class.java.getResource("/$fileName").readText()
    }

    private fun responseBodyFromJson(fileName: String): MockResponse {
        return MockResponse().setBody(readJson(fileName))
    }

}