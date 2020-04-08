package xyz.schnabl.remote.savings

import xyz.schnabl.remote.AmountDto
import java.util.UUID

/**
 * Represents the RequestBody returned when the savings goal resource is queried
 */
data class SavingsGoalInfoDto (
    val savingsGoalUid: UUID,
    val name: String,
    val target: AmountDto,
    val totalSaved: AmountDto,
    val savedPercentage: Byte
)