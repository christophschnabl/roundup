package xyz.schnabl

import com.google.inject.Guice
import com.google.inject.Injector

/**
 * TODO kdco
 */
fun main() {
    val injector: Injector = Guice.createInjector(RoundupModule())

    // Take User Input parameter
    // Get Accounts for User - simple accounts request
    // Get Transactions for User for all accounts (since the beginning of the last week) - calculate date request takes the parameter

    val client = injector.getInstance(StarlingClient::class.java)
    println(client.getAccountsForUser())
}