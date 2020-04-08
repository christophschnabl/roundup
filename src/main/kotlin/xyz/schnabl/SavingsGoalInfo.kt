package xyz.schnabl

import java.util.UUID

data class SavingsGoalInfo (
    val savingsGoalUid: UUID,
    val name: String,
    val target: Amount,
    val totalSaved: Amount,
    val savedPercentage: Byte
)