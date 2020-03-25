package xyz.schnabl.remote

import java.util.Currency

/**
 * KDOC
 */
data class AmountDto (
    val currency: Currency,
    val minorUnits: Long
)