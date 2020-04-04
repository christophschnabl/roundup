package xyz.schnabl

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.name.Named
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.schnabl.remote.StarlingClient


class RoundupIntegrationTest {
    private val server = MockWebServer()

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
    fun h() {
        server.enqueue(MockResponse().setBody("{\n" +
                "\"accounts\": [\n" +
                "        {\n" +
                "            \"accountUid\": \"a7ab343e-ef3e-4e39-a850-2d0c5bb44cde\",\n" +
                "            \"defaultCategory\": \"0791a3a1-487c-4998-b11b-3852dc833ed3\",\n" +
                "            \"currency\": \"GBP\",\n" +
                "            \"createdAt\": \"2020-03-22T19:33:48.990Z\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"))

        val a = client.getAccountsForUser()
        println(a)
    }
            // server = new MockWebServer();

    @Test
    fun t() {

    }
    // Schedule some responses.
    //server.enqueue(new MockResponse().setBody("hello, world!"));
}