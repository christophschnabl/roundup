package xyz.schnabl.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.inject.name.Named
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.account.AccountsDto
import xyz.schnabl.remote.feed.TransactionFeedDto
import xyz.schnabl.remote.savings.CreateSavingsGoalDto
import xyz.schnabl.remote.savings.SavingsGoalDto
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Currency
import java.util.UUID


private val GBP = Currency.getInstance("GBP")

/**
 * TODO KDOC - TODO client interface
 */
@Singleton
class StarlingClient @Inject constructor(
    private val client: OkHttpClient,
    @Named("baseUrl") private val url: String, // TODO inject config class
    @Named("accountsEndpoint") private val accountsEndpoint: String,
    @Named("feedEndpoint") private val feedEndpoint: String,
    @Named("categoryEndpoint") private val categoryEndpoint: String,
    @Named("savingsGoalsEndpoint") private val savingsGoalsEndpoint: String,
    @Named("accountEndpoint") private val accountEndpoint: String,
    @Named("addMoneyEndpoint") private val addMoneyEndpoint: String
) {

    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java,
        JsonDeserializer { json: JsonElement, _, _ ->
            ZonedDateTime.parse(
                json.asJsonPrimitive.asString
            ).toLocalDateTime()
        } as JsonDeserializer<LocalDateTime>
    ).create() // TODO provide and inject json if needed in other components

    // TODO refactor http interactions

    /**
     * TODO KDOC
     */
    fun getAccountsForUser(): List<AccountDto> {
        return getResourceForEndpoint(buildRequest(accountsEndpoint).build(), AccountsDto::class.java).accounts
    }

    /**
     * TODO KDOC
     */
    fun getTransactionsForAccountByCategory(
        accountUid: UUID,
        categoryUid: UUID,
        changesSince: LocalDateTime
    ): TransactionFeedDto { // TODO error handling

        val transactionFeedResourceEndpoint =
            "$feedEndpoint/$accountEndpoint/$accountUid/$categoryEndpoint/$categoryUid"
        val params = "changesSince=${changesSince.toInstant(ZoneOffset.UTC)}"

        val request = buildRequest("$transactionFeedResourceEndpoint?$params").build()

        return getResourceForEndpoint(request, TransactionFeedDto::class.java)
    }

    /**
     * TODO KDOC
     */
    fun createSavingsGoal(accountUid: UUID, name: String, target: Long): SavingsGoalDto {
        val savingsGoalsEndpoint = "$accountEndpoint/$accountUid/$savingsGoalsEndpoint" // TODO refactor
        val savingsGoalDto = CreateSavingsGoalDto(name, GBP, AmountDto(GBP, target))

        val body = createRequestBodyFromDto(savingsGoalDto)
        val request = buildRequest(savingsGoalsEndpoint).put(body).build()

        return getResourceForEndpoint(request, SavingsGoalDto::class.java) // TODO extension function
    }

    /**
     * TODO KDOC
     */
    fun transferToSavingsGoal(accountUid: UUID, savingsGoalUid: UUID, transferUid: UUID) {
        val savingsGoalsEndpoint = "$accountEndpoint/$accountUid/$savingsGoalsEndpoint/$addMoneyEndpoint" // TODO this is sduplicated
    }

    private fun <T> createRequestBodyFromDto(obj: T): RequestBody {
        return gson.toJson(obj).toRequestBody("application/json".toMediaType())
    }

    private fun <T> getResourceForEndpoint(request: Request, resourceClass: Class<T>): T {
        val response = executeRequest(request)
        return parseResponseBody(response, resourceClass)
    }

    private fun buildRequest(endpoint: String): Request.Builder {
        return Request.Builder().url("$url/$endpoint")
    }

    private fun executeRequest(request: Request): String {
        val response = client.newCall(request).execute()
        return response.body?.string() ?: throw Exception("Could not access response body") // TODO more logging and handle 404s
    }

    private fun <T> parseResponseBody(body: String, resourceClass: Class<T>): T {
        return gson.fromJson(body, resourceClass)
    }

}