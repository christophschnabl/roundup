package xyz.schnabl.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.inject.name.Named
import okhttp3.OkHttpClient
import okhttp3.Request
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.account.AccountsDto
import java.time.LocalDateTime
import java.time.ZonedDateTime


/**
 * TODO KDOC - TODO client interface
 */
@Singleton
class StarlingClient @Inject constructor(
    private val client: OkHttpClient,
    @Named("baseUrl") private val url: String,
    @Named("accountsEndpoint") private val accountsEndpoint: String,
    @Named("feedEndpoint") private val feedEndpoint: String,
    @Named("categoryEndpoint") private val categoryEndpoint: String
) {

    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java,
        JsonDeserializer { json: JsonElement, _, _ ->
            ZonedDateTime.parse(
                json.asJsonPrimitive.asString
            ).toLocalDateTime()
        } as JsonDeserializer<LocalDateTime>
    ).create()

    /**
     * TODO KDOC
     */
    fun getAccountsForUser(): List<AccountDto> {
        return getResourceForEndpoint(accountsEndpoint, AccountsDto::class.java).accounts
    }

    /**
     * TODO KDOC
     */
    fun getTransactionsForAccountByCategory(accountUid: String, categoryUid: String, changesSince: LocalDateTime) {
        val transactionFeedResourceEndpoint = "$feedEndpoint/$accountsEndpoint/$accountUid/$categoryEndpoint/$categoryUid"
        //val params = "?changesSince"

        //return getResourceForEndpoint(transactionFeedResourceEndpoint, TransactionFeedDto params)
    }


    private fun <T> getResourceForEndpoint(endpoint: String, resourceClass: Class<T>, requestParams: String = "") : T {
        val request = buildRequest("$url/$endpoint")
        val response = executeRequest(request)
        return parseResponseBody(response, resourceClass)
    }

    private fun buildRequest(url: String) : Request {
        return Request.Builder().url(url).build()
    }

    private fun executeRequest(request: Request): String {
        val response = client.newCall(request).execute()
        return response.body?.string()?: throw Exception("Could not access response body") // TODO more logging
    }

    private fun<T> parseResponseBody(body: String, resourceClass: Class<T>): T {
        return gson.fromJson(body, resourceClass)
    }

}