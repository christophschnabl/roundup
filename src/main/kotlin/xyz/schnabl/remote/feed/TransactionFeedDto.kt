package xyz.schnabl.remote.feed

import xyz.schnabl.remote.AmountDto
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
 * TODO KDOC
 */
data class FeedItemDto (
    val feedItemUid: UUID,
    val categoryUid: UUID,
    val amount: AmountDto,
    val direction: TransactionDirection
)