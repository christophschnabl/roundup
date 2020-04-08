package xyz.schnabl.remote.savings

import xyz.schnabl.remote.ErrorDto
import java.util.UUID

/**
 * Represents the ResponseBody of a savings goal transfer
 */
data class TransferSavingsGoalDto (
    val transferUid: UUID,
    val success: Boolean,
    val errors: List<ErrorDto>
)