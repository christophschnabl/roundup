package xyz.schnabl.remote

import com.google.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.schnabl.Config
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.account.AccountsDto
import xyz.schnabl.remote.feed.TransactionFeedDto
import xyz.schnabl.remote.savings.CreateSavingsGoalDto
import xyz.schnabl.remote.savings.CreateSavingsTransferDto
import xyz.schnabl.remote.savings.SavingsGoalDto
import xyz.schnabl.remote.savings.SavingsGoalInfoDto
import xyz.schnabl.remote.savings.TransferSavingsGoalDto
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Currency
import java.util.UUID

private val GBP = Currency.getInstance("GBP")

/**
 * Implements the StarlingClient using OKHttp
 * @property client: OkHttpClient - as configured in the module
 * @property config: Config - the application properties wrapped in a config
 * @property json: JsonSerdeService - the service provided to serialize and deserialize objects to and from json
 */
class StarlingClientImpl @Inject constructor(
    private val client: OkHttpClient,
    private val config: Config,
    private val json: JsonSerdeService
) : StarlingClient {

    override fun getAccountsForUser(): List<AccountDto> {
        return getResourceForEndpoint(buildRequest(config.accountsEndpoint).build(), AccountsDto::class.java).accounts
    }

    override fun getTransactionsForAccountByCategory(
        accountUid: UUID,
        categoryUid: UUID,
        changesSince: LocalDateTime
    ): TransactionFeedDto {

        val transactionFeedResourceEndpoint =
            "${config.feedEndpoint}/${config.accountEndpoint}/$accountUid/${config.categoryEndpoint}/$categoryUid"

        val params = "changesSince=${changesSince.toInstant(ZoneOffset.UTC)}"

        val request = buildRequest("$transactionFeedResourceEndpoint?$params").build()

        return getResourceForEndpoint(request, TransactionFeedDto::class.java)
    }

    override fun createSavingsGoal(accountUid: UUID, name: String, target: Long): SavingsGoalDto {
        val savingsGoalsEndpoint = "${config.accountEndpoint}/$accountUid/${config.savingsGoalsEndpoint}"
        val savingsGoalDto = CreateSavingsGoalDto(name, GBP, AmountDto(GBP, target))

        val body = createRequestBodyFromDto(savingsGoalDto)
        val request = buildRequest(savingsGoalsEndpoint).put(body).build()

        return getResourceForEndpoint(request, SavingsGoalDto::class.java)
    }

    override  fun transferToSavingsGoal(accountUid: UUID, savingsGoalUid: UUID, transferUid: UUID, amount: Long): TransferSavingsGoalDto {
        val savingsGoalsEndpoint = "${config.accountEndpoint}/$accountUid/${config.savingsGoalsEndpoint}/$savingsGoalUid/${config.addMoneyEndpoint}/$transferUid"
        val amountDto = CreateSavingsTransferDto(AmountDto(GBP, amount))

        val body = createRequestBodyFromDto(amountDto)
        val request = buildRequest(savingsGoalsEndpoint).put(body).build()

        return getResourceForEndpoint(request, TransferSavingsGoalDto::class.java)
    }

    override fun getSavingsGoal(accountUid: UUID, savingsGoalUid: UUID) : SavingsGoalInfoDto {
        return getResourceForEndpoint(buildRequest("${config.accountEndpoint}/$accountUid/${config.savingsGoalsEndpoint}/$savingsGoalUid").build(), SavingsGoalInfoDto::class.java)
    }

    private fun <T> createRequestBodyFromDto(obj: T): RequestBody {
        return json.toJson(obj).toRequestBody("application/json".toMediaType())
    }

    private fun <T> getResourceForEndpoint(request: Request, resourceClass: Class<T>): T {
        val response = executeRequest(request)
        return parseResponseBody(response, resourceClass)
    }

    private fun buildRequest(endpoint: String): Request.Builder {
        return Request.Builder().url("${config.url}/$endpoint")
    }

    private fun executeRequest(request: Request): String {
        val response = client.newCall(request).execute()

        if (response.code != 200 && response.code != 201) {
            throw IllegalStateException("Request unsuccessful with response (${response.body?.string()}) and code (${response.code}) for request ($request)")
        }

        return response.body?.string() ?: throw Exception("Could not access response body")
    }

    private fun <T> parseResponseBody(body: String, resourceClass: Class<T>): T {
        return json.fromJson(body, resourceClass)
    }
}