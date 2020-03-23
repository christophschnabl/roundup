package xyz.schnabl.remote.account.transactionfeed

import java.time.LocalDateTime

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
    val accountUid : String, // TODO UUID?
    val defaultCategory : String, // TODO UUID?
    val currency: String, // TODO CUrrency?
    val createdAt: LocalDateTime
)