package xyz.schnabl

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.name.Named
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.schnabl.remote.AmountDto
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.account.AccountsDto
import xyz.schnabl.remote.feed.FeedItemDto
import xyz.schnabl.remote.feed.TransactionDirection
import xyz.schnabl.remote.feed.TransactionFeedDto
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals


class RoundupIntegrationTest {
    private val server = MockWebServer()

    private val GBP = Currency.getInstance("GBP")
    private val accountUid = UUID.fromString("a7ab343e-ef3e-4e39-a850-2d0c5bb44cde")
    private val defaultCategory = UUID.fromString("0791a3a1-487c-4998-b11b-3852dc833ed3")
    private val expectedAccount = AccountDto(accountUid, defaultCategory, GBP, parseTime("2020-03-22T19:33:48.990Z"))

    private lateinit var injector: Injector
    private lateinit var client : StarlingClient

    @Before
    fun setUp() {
        server.start()

        injector = Guice.createInjector(object : AbstractModule() {
            @Provides
            fun provideConfig(): Config {
                return Config("http://${server.hostName}:${server.port}", "", "", "", "", "", "")
            }
        })

        client = injector.getInstance(StarlingClient::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `testGETAccountsForUserHappyPath`() {
        val expected = listOf(expectedAccount)

        // Given
        server.enqueue(responseBodyFromJson("account.json"))

        // When
        val actual = client.getAccountsForUser()

        // Then
        assertEquals(expected, actual)
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

        val expectedFeed = TransactionFeedDto(listOf(feedItem1, feedItem2))


        server.enqueue(responseBodyFromJson("feed.json"))

        val actualFeed = client.getTransactionsForAccountByCategory(expectedAccount.accountUid, expectedAccount.defaultCategory, LocalDateTime.now())

        assertEquals(expectedFeed, actualFeed)
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
}