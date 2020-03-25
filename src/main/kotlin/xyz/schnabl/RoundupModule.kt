package xyz.schnabl

import com.google.inject.AbstractModule
import com.google.inject.Provides
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
        // LOG IF NOT SUCCESSFUL
        return OkHttpClient().newBuilder().addNetworkInterceptor(AuthenticationInterceptor()).build()
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
