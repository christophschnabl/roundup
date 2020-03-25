package xyz.schnabl

import com.google.inject.Guice
import com.google.inject.Injector
import xyz.schnabl.remote.StarlingClient
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
    val now = LocalDateTime.now()

    val firstDayOfWeek = now.toStartDate().minusDays(now.dayOfWeek.value - 1L).toStartDate()

    println(firstDayOfWeek.toInstant(ZoneOffset.UTC))

    val accounts = client.getAccountsForUser()

    accounts.forEach {
        println(client.getTransactionsForAccountByCategory(it.accountUid, it.defaultCategory, firstDayOfWeek))
    }

    client.createSavingsGoal(accounts[0].accountUid, "journey", 100).also { println(it) }
}

/**
 * Sets the hours, minutes, etc. to zero by subtracting its current value
 */
fun LocalDateTime.toStartDate(): LocalDateTime {
    return minusHours(this.hour * 1L).minusMinutes(this.minute * 1L).minusSeconds(this.second * 1L)
        .minusNanos(this.nano * 1L)
}