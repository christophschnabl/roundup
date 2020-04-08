package xyz.schnabl

import java.util.UUID

/**
 * TODO
 */
interface RoundupService {

    /**
     *
     */
    fun getAllOutgoingTransactionsForFirstAccount(): Pair<UUID, List<Transaction>>

    /**
     *
     */
    fun getRoundupSumForTransactions(transactions: List<Transaction>) : Long

    /**
     *
     */
    fun createAndTransferToSavingsGoal(accountUid: UUID, name: String, target: Long, amount: Long) : SavingsGoalInfo
}