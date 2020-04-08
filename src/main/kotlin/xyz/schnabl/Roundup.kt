package xyz.schnabl

import com.google.inject.Guice
import com.google.inject.Injector


/**
 * The main method runs the round up directly for the provided bearer authentication token as
 * environment variable (e.g. STARLING_TOKEN=ey...). The first account for this user is taken into consideration and
 * a savings goal for this week is created with a provided name and target. A new goal is created each time as this application is stateless,
 * but could easily be adapted.
 */
fun main() {
    val injector: Injector = Guice.createInjector(RoundupModule())
    val roundupService = injector.getInstance(RoundupServiceImpl::class.java)

    val name = "Flight to Vienna"
    val target = 10000L

    val transactionsForAccount = roundupService.getAllOutgoingTransactionsForFirstAccount()
    val roundUp = roundupService.getRoundupSumForTransactions(transactionsForAccount.second)
    val savingsGoalInfo = roundupService.createAndTransferToSavingsGoal(transactionsForAccount.first, name, target, roundUp)

    println("Your current savings goal progress is ${savingsGoalInfo.totalSaved.minorUnits}/${savingsGoalInfo.target.minorUnits} (${savingsGoalInfo.savedPercentage}%)")
}
