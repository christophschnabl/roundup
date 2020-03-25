package xyz.schnabl.remote.account

import java.time.LocalDateTime
import java.util.*

/**
 * TODO kdoc
 */
data class AccountsDto (
    val accounts: List<AccountDto>
)

/**
 * TODO kdoc
 */
data class AccountDto (
    val accountUid : UUID,
    val defaultCategory : UUID,
    val currency: Currency,
    val createdAt: LocalDateTime
)