package xyz.schnabl.remote.savings

import xyz.schnabl.remote.ErrorDto
import java.util.UUID

/**
 * Represents the ResponseBody of a SavingsGoal
 */
data class SavingsGoalDto(
    val savingsGoalUid: UUID,
    val success: Boolean,
    val errors: List<ErrorDto>
)