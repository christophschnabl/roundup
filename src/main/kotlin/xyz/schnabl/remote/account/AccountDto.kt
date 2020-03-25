package xyz.schnabl.remote.account

import java.time.LocalDateTime
import java.util.UUID

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
    val accountUid : UUID, // TODO UUID?
    val defaultCategory : String, // TODO UUID?
    val currency: String, // TODO CUrrency?
    val createdAt: LocalDateTime
)