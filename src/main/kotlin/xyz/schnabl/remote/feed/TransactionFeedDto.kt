package xyz.schnabl.remote.feed

import xyz.schnabl.remote.AmountDto
import java.time.LocalDateTime
import java.util.UUID

enum class TransactionDirection {
    IN,
    OUT
}

/**
 * TODO KDOC
 */
data class TransactionFeedDto (
    val feedItems: List<FeedItemDto>
)


/**
 * only the properties needed are depicted
 */
data class FeedItemDto (
    val feedItemUid: UUID,
    val categoryUid: UUID,
    val amount: AmountDto,
    val direction: TransactionDirection,
    val transactionTime: LocalDateTime, // TODO not needed
    val settlementTime: LocalDateTime // TODO not needed
)