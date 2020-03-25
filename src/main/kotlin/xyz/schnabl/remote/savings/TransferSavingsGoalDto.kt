package xyz.schnabl.remote.savings

import xyz.schnabl.ErrorDto
import java.util.UUID

data class TransferSavingsGoalDto (
    val transferUid: UUID,
    val success: Boolean,
    val errors: List<ErrorDto>
)