package xyz.schnabl

import java.util.Currency
import java.util.UUID


data class Amount (
    val currency: Currency,
    val minorUnits: Long
)

data class Transaction (
    val feedItemUid: UUID,
    val amount: Amount
)