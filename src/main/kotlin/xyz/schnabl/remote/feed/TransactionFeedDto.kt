package xyz.schnabl.remote.feed

import xyz.schnabl.remote.AmountDto
import java.time.LocalDateTime
import java.util.UUID

enum class TransactionDirection {
    IN,
    OUT
}

enum class Source {
    CASH_DEPOSIT,
    CASH_DEPOSIT_CHARGE,
    CASH_WITHDRAWAL,
    CASH_WITHDRAWAL_CHARGE,
    CHAPS, CHEQUE,
    CICS_CHEQUE,
    CURRENCY_CLOUD,
    DIRECT_CREDIT,
    DIRECT_DEBIT,
    DIRECT_DEBIT_DISPUTE,
    INTERNAL_TRANSFER,
    MASTER_CARD,
    MASTERCARD_MONEYSEND,
    MASTERCARD_CHARGEBACK,
    FASTER_PAYMENTS_IN,
    FASTER_PAYMENTS_OUT,
    FASTER_PAYMENTS_REVERSAL,
    STRIPE_FUNDING,
    INTEREST_PAYMENT,
    NOSTRO_DEPOSIT,
    OVERDRAFT,
    OVERDRAFT_INTEREST_WAIVED,
    FASTER_PAYMENTS_REFUND,
    STARLING_PAY_STRIPE,
    ON_US_PAY_ME,
    LOAN_PRINCIPAL_PAYMENT,
    LOAN_REPAYMENT,
    LOAN_OVERPAYMENT,
    LOAN_LATE_PAYMENT,
    SEPA_CREDIT_TRANSFER,
    SEPA_DIRECT_DEBIT,
    TARGET2_CUSTOMER_PAYMENT,
    FX_TRANSFER,
    ISS_PAYMENT,
    STARLING_PAYMENT,
    SUBSCRIPTION_CHARGE,
    OVERDRAFT_FEE
}


/**
 * Wraps the FeedItems in a list with a key needed for the de/serialization
 */
data class TransactionFeedDto(
    val feedItems: List<FeedItemDto>
)


/**
 * Represents the FeedItem Resource of the Starling API
 * only the properties needed for this application are present here
 */
data class FeedItemDto(
    val feedItemUid: UUID,
    val categoryUid: UUID,
    val amount: AmountDto,
    val direction: TransactionDirection,
    val transactionTime: LocalDateTime,
    val settlementTime: LocalDateTime,
    val source: Source
)