package xyz.schnabl

import com.google.inject.Inject
import com.google.inject.Singleton
import xyz.schnabl.remote.AmountDto
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.feed.FeedItemDto
import xyz.schnabl.remote.feed.Source
import xyz.schnabl.remote.feed.TransactionDirection
import xyz.schnabl.remote.savings.SavingsGoalInfoDto
import java.time.LocalDateTime
import java.util.UUID

/**
 * Implements the RoundupService
 */
@Singleton
class RoundupServiceImpl @Inject constructor(private val client: StarlingClient) : RoundupService {

    override fun getAllOutgoingTransactionsForFirstAccount(): Pair<UUID, List<Transaction>> {
        val firstDayOfWeek = getFirstDayOfWeek()

        val accounts = client.getAccountsForUser()
        println("Got the following accounts for the user: $accounts")

        val outgoingTransactionsForAllAccounts = accounts.map { account ->
            val transactions =
                client.getTransactionsForAccountByCategory(account.accountUid, account.defaultCategory, firstDayOfWeek)
            transactions.feedItems.filter { isOutgoing(it) }.filter { !isInternal(it) }
        }.flatten()

        println("Found ${outgoingTransactionsForAllAccounts.size} outgoing transactions for all accounts since $firstDayOfWeek")

        return Pair(accounts.first().accountUid, outgoingTransactionsForAllAccounts.map { it.toTransaction() })
    }

    override fun getRoundupSumForTransactions(transactions: List<Transaction>): Long {
        return transactions.map {
            val roundUp = roundUp(it.amount.minorUnits)
            println("Rounding ${it.amount.minorUnits} up by $roundUp to a total of ${it.amount.minorUnits + roundUp}")
            roundUp
        }.sum().also { println("Total round up is $it") }
    }

    override fun createAndTransferToSavingsGoal(
        accountUid: UUID,
        name: String,
        target: Long,
        amount: Long
    ): SavingsGoalInfo {
        val savingsGoal = client.createSavingsGoal(accountUid, name, target)
        println("Creating a new savings goal $savingsGoal")

        if (amount > 0) {
            val transferUid =
                client.transferToSavingsGoal(
                    accountUid,
                    savingsGoal.savingsGoalUid,
                    UUID.randomUUID(),
                    amount
                ).transferUid
            println("Transferring $amount to the following savings goal ${savingsGoal.savingsGoalUid} with transaction: $transferUid")
        } else {
            println("No transfer is created for an amount $amount <= 0")
        }

        return client.getSavingsGoal(accountUid, savingsGoal.savingsGoalUid).toInfo()
    }

    private fun roundUp(value: Long): Long {
        return (100 * (value / 100) + 100) - value
    }

    private fun isOutgoing(feedItem: FeedItemDto): Boolean {
        return feedItem.direction == TransactionDirection.OUT
    }

    private fun isInternal(feedItem: FeedItemDto): Boolean {
        return feedItem.source == Source.INTERNAL_TRANSFER
    }

    private fun getFirstDayOfWeek(): LocalDateTime {
        val now = LocalDateTime.now()

        return now.toStartDate().minusDays(now.dayOfWeek.value - 1L).toStartDate()
    }

    private fun FeedItemDto.toTransaction(): Transaction {
        return Transaction(feedItemUid, amount.toAmount())
    }

    private fun AmountDto.toAmount(): Amount {
        return Amount(currency, minorUnits)
    }

    private fun SavingsGoalInfoDto.toInfo(): SavingsGoalInfo {
        return SavingsGoalInfo(savingsGoalUid, name, target.toAmount(), totalSaved.toAmount(), savedPercentage)
    }

    /**
     * Sets the hours, minutes, etc. to zero by subtracting its current value
     */
    private fun LocalDateTime.toStartDate(): LocalDateTime {
        return minusHours(this.hour * 1L).minusMinutes(this.minute * 1L).minusSeconds(this.second * 1L)
            .minusNanos(this.nano * 1L)
    }

}