package xyz.schnabl

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Named
import com.google.inject.name.Names
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.FileReader
import java.io.IOException
import java.util.*


internal class AuthenticationInterceptor : Interceptor {
    // @Throws(IOException::class) TODO needed?
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val bearerToken = "Bearer " + System.getenv("STARLING_TOKEN") // TODO is this not nullable?
        // TODO read name of env from config

        // TODO add header constants/ is there some sort of enum
        val request = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Chris S.")
            .addHeader("Authorization", bearerToken)
            .build()

        return chain.proceed(request)
    }
}

data class Config( //TODO rethink this
    val url: String,
    val accountsEndpoint: String,
    val feedEndpoint: String,
    val categoryEndpoint: String,
    val savingsGoalsEndpoint: String,
    val accountEndpoint: String,
    val addMoneyEndpoint: String
)


class RoundupModule : AbstractModule() {
    override fun configure() {
        loadProperties()

        // bind stuff here
    }

    @Provides
    fun provideHttpClient(): OkHttpClient {
        // TODO log incoming requests
        // TODO circuit breaker?
        // TODO network or simple interceptor
        // TODO authentication failed
        // LOG IF NOT SUCCESSFUL
        return OkHttpClient().newBuilder().addNetworkInterceptor(AuthenticationInterceptor()).build()
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
        return Config(url,accountsEndpoint, feedEndpoint, categoryEndpoint, savingsGoalsEndpoint, accountEndpoint, addMoneyEndpoint)
    }


    private fun loadProperties() {
        try {
            val properties = Properties()
            val configFileName = RoundupModule::class.java.classLoader.getResource("application.properties")
            properties.load(FileReader(configFileName.file)) // TODO OR DESERIALIZE Conf klass
            Names.bindProperties(binder(), properties)
        } catch (ex: IOException) {
            // TODO logger error here and exit application
            ex.printStackTrace()
        }
    }

}
