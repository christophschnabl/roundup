package xyz.schnabl

import com.google.inject.Guice
import com.google.inject.Injector
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.feed.TransactionDirection
import java.time.LocalDateTime
import java.time.ZoneOffset


/**
 * TODO kdco
 */
fun main() {
    val injector: Injector = Guice.createInjector(RoundupModule())

    // Take User Input parameter
    // Get Accounts for User - simple accounts request
    // Get Transactions for User for all accounts (since the beginning of the last week) - calculate date request takes the parameter

    val client = injector.getInstance(StarlingClient::class.java)
    val now = LocalDateTime.now().minusDays(10)
    val savingsGoalName = ""

    val firstDayOfWeek = now.toStartDate().minusDays(now.dayOfWeek.value - 1L).toStartDate()

    println(firstDayOfWeek.toInstant(ZoneOffset.UTC))

    val accounts = client.getAccountsForUser()

    val outgoingTransactionsForAllAccounts = accounts.map { account ->
        println(client.getTransactionsForAccountByCategory(account.accountUid, account.defaultCategory, firstDayOfWeek))
        val transactions = client.getTransactionsForAccountByCategory(account.accountUid, account.defaultCategory, firstDayOfWeek)
        transactions.feedItems.filter {feedItem ->
            feedItem.direction == TransactionDirection.OUT
        }.map { feedItem ->
            feedItem
        }
    }.flatten()

    val amountToTopUp = outgoingTransactionsForAllAccounts.map { transaction ->
        transaction.amount.minorUnits
    }.also {
        println(it)
    }.map {amount ->
        (100 * (amount / 100) + 100) - amount
    }.also {
        println(it)
    }.sum()
    // TODO add info when printing

    // val savingsGoalDto = client.createSavingsGoal(accounts[0].accountUid, "journey", 100).also { println(it) }
    // TODO insufficient funds?
    // TODO when to create a savings goal
    // client.transferToSavingsGoal(accounts[0].accountUid, savingsGoalDto.savingsGoalUid, UUID.randomUUID(), 10).also { println(it) }
}

// TODO Documentation or source code comments to help your reviewer orient themselves will also be appreciated!


/**
 * Sets the hours, minutes, etc. to zero by subtracting its current value
 */
fun LocalDateTime.toStartDate(): LocalDateTime {
    return minusHours(this.hour * 1L).minusMinutes(this.minute * 1L).minusSeconds(this.second * 1L)
        .minusNanos(this.nano * 1L)
}