package xyz.schnabl.remote

import java.util.Currency

/**
 * Represents the Amount
 */
data class AmountDto (
    val currency: Currency,
    val minorUnits: Long
)