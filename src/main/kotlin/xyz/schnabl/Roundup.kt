package xyz.schnabl

import com.google.inject.Guice
import com.google.inject.Injector
import xyz.schnabl.remote.account.transactionfeed.StarlingClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * TODO kdco
 */
    fun main() {
    val injector: Injector = Guice.createInjector(RoundupModule())

    // Take User Input parameter
    // Get Accounts for User - simple accounts request
    // Get Transactions for User for all accounts (since the beginning of the last week) - calculate date request takes the parameter

    val client = injector.getInstance(StarlingClient::class.java)
    val now = LocalDateTime.now().plusDays(1)

    val firstDayOfWeek = now.minusDays(now.dayOfWeek.value - 1L)
    println(firstDayOfWeek.toInstant(ZoneOffset.UTC))
    //println(client.getAccountsForUser())
}