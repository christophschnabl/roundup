package xyz.schnabl

import java.util.UUID

/**
 * Defines the actions to perform the roundup feature
 */
interface RoundupService {

    /**
     * Returns all Transactions for the first account of the user for the token configured
     * @return the accounts uuid and transactions
     */
    fun getAllOutgoingTransactionsForFirstAccount(): Pair<UUID, List<Transaction>>

    /**
     * Calculates the roundup for the provided transactions
     * @param transactions list of the transactions
     * @return the roundup sum
     */
    fun getRoundupSumForTransactions(transactions: List<Transaction>) : Long

    /**
     * Creates a saving goal each time and transfers the given amount to that goal
     * @param accountUid UUID of the account
     * @param name name of the savings goal
     * @param target to reach
     * @param amount amount to top up
     */
    fun createAndTransferToSavingsGoal(accountUid: UUID, name: String, target: Long, amount: Long) : SavingsGoalInfo
}