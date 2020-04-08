package xyz.schnabl.remote.savings

import xyz.schnabl.remote.AmountDto

/**
 * Specifies the RequestBody for transfers to a savings goal
 */

data class CreateSavingsTransferDto(
    val amount: AmountDto
)