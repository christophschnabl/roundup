package xyz.schnabl.remote.account

import java.time.LocalDateTime
import java.util.UUID
import java.util.Currency

/**
 * Wraps accounts in a list with a key needed for the serialization
 */
data class AccountsDto (
    val accounts: List<AccountDto>
)

/**
 * Represents an Account as specified in the Starling API
 */
data class AccountDto (
    val accountUid : UUID,
    val defaultCategory : UUID,
    val currency: Currency,
    val createdAt: LocalDateTime
)