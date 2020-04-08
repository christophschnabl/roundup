package xyz.schnabl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.name.Named
import com.google.inject.name.Names
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import xyz.schnabl.remote.GsonSerdeService
import xyz.schnabl.remote.JsonSerdeService
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.StarlingClientImpl
import java.io.FileReader
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Properties


internal class AuthenticationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val bearerToken = "Bearer " + System.getenv("STARLING_TOKEN")

        // TODO add header constants/ is there some sort of enum
        val request = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Chris S.")
            .addHeader("Authorization", bearerToken)
            .build()

        return chain.proceed(request)
    }
}

class RoundupModule : AbstractModule() {
    override fun configure() {
        bind(StarlingClient::class.java).to(StarlingClientImpl::class.java)
        bind(RoundupService::class.java).to(RoundupServiceImpl::class.java)
        bind(JsonSerdeService::class.java).to(GsonSerdeService::class.java)
        loadProperties()
    }

    @Provides
    fun provideHttpClient(): OkHttpClient {
        // Add to readme
        // TODO log incoming requests
        // TODO circuit breaker?
        // TODO network or simple interceptor
        // TODO authentication failed
        // TODO log
        return OkHttpClient().newBuilder().addNetworkInterceptor(AuthenticationInterceptor()).build()
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder().registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json: JsonElement, _, _ ->
                    ZonedDateTime.parse(
                        json.asJsonPrimitive.asString
                    ).toLocalDateTime()
                } as JsonDeserializer<LocalDateTime>
        ).create()
    }

    @Provides
    fun provideConfig(
        @Named("baseUrl") url: String,
        @Named("accountsEndpoint") accountsEndpoint: String,
        @Named("feedEndpoint") feedEndpoint: String,
        @Named("categoryEndpoint") categoryEndpoint: String,
        @Named("savingsGoalsEndpoint") savingsGoalsEndpoint: String,
        @Named("accountEndpoint") accountEndpoint: String,
        @Named("addMoneyEndpoint") addMoneyEndpoint: String
    ): Config {
        return Config(url, accountsEndpoint, feedEndpoint, categoryEndpoint, savingsGoalsEndpoint, accountEndpoint, addMoneyEndpoint)
    }


    private fun loadProperties() {
        try {
            val properties = Properties()
            val configFileName = RoundupModule::class.java.classLoader.getResourceAsStream("application.properties")
            properties.load(configFileName)
            Names.bindProperties(binder(), properties)
        } catch (ex: IOException) {
            // TODO logger error here and exit application ->
            ex.printStackTrace()
        }
    }

}
