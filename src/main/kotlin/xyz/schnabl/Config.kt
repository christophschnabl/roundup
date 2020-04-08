package xyz.schnabl

/**
 * Config for the application.properties
 */
data class Config(
    val url: String,
    val accountsEndpoint: String,
    val feedEndpoint: String,
    val categoryEndpoint: String,
    val savingsGoalsEndpoint: String,
    val accountEndpoint: String,
    val addMoneyEndpoint: String
)
