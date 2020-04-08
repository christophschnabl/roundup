package xyz.schnabl


import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import xyz.schnabl.remote.AmountDto
import xyz.schnabl.remote.StarlingClient
import xyz.schnabl.remote.account.AccountDto
import xyz.schnabl.remote.feed.FeedItemDto
import xyz.schnabl.remote.feed.Source
import xyz.schnabl.remote.feed.TransactionDirection
import xyz.schnabl.remote.feed.TransactionFeedDto
import xyz.schnabl.remote.savings.SavingsGoalDto
import xyz.schnabl.remote.savings.SavingsGoalInfoDto
import xyz.schnabl.remote.savings.TransferSavingsGoalDto
import java.time.LocalDateTime
import java.util.Currency
import java.util.UUID
import kotlin.test.assertEquals


class RoundupServiceImplTest {

    private val accountUid = UUID.randomUUID()
    private val GBP = Currency.getInstance("GBP")
    private val account = AccountDto(accountUid, UUID.randomUUID(), GBP, LocalDateTime.now())
    private val now = LocalDateTime.now()

    private val transactionList = TransactionFeedDto(listOf(
            FeedItemDto(UUID.randomUUID(), UUID.randomUUID(), AmountDto(GBP,4242), TransactionDirection.OUT, now, now, Source.CASH_DEPOSIT),
            FeedItemDto(UUID.randomUUID(), UUID.randomUUID(), AmountDto(GBP,1337), TransactionDirection.OUT, now, now, Source.INTERNAL_TRANSFER),
            FeedItemDto(UUID.randomUUID(), UUID.randomUUID(), AmountDto(GBP,1234), TransactionDirection.OUT, now, now, Source.FASTER_PAYMENTS_OUT),
            FeedItemDto(UUID.randomUUID(), UUID.randomUUID(), AmountDto(GBP,1000), TransactionDirection.IN, now, now, Source.FASTER_PAYMENTS_IN)
    ))

    private val savingsGoalDto = SavingsGoalDto(UUID.randomUUID(), true, listOf())

    private val client = mock<StarlingClient> {
        on { getAccountsForUser() } doReturn listOf(account)
        on { getTransactionsForAccountByCategory(eq(accountUid), eq(account.defaultCategory), any())} doReturn transactionList
        on { createSavingsGoal(eq(accountUid), any(), any()) } doReturn savingsGoalDto
        on { transferToSavingsGoal(any(), any(), any(), any())} doReturn TransferSavingsGoalDto(UUID.randomUUID(), true, listOf())
        on { getSavingsGoal(any(), any())} doReturn SavingsGoalInfoDto(UUID.randomUUID(), "Investment", AmountDto(GBP, 1000), AmountDto(GBP, 124), 12)
    }

    @Test
    fun `GivenAListOfTransactionsWhenAnAccountIsQueriedAListIsReturned`() {
        val roundupService = RoundupServiceImpl(client)

        val outgoingTransactions = roundupService.getAllOutgoingTransactionsForFirstAccount()

        assertEquals(accountUid, outgoingTransactions.first)
        assertEquals(2, outgoingTransactions.second.size)
    }

    @Test
    fun `GivenAListOfTransactionsWhenARoundupIsInvoked`() {
        val roundupService = RoundupServiceImpl(client)

        val outgoingTransactions = roundupService.getAllOutgoingTransactionsForFirstAccount()

        val roundUp = roundupService.getRoundupSumForTransactions(outgoingTransactions.second)

        assertEquals(124, roundUp)
    }

    @Test
    fun `GivenAnEmptyListOfTransactionsWhenARoundupIsInvokedThenZero`() {
        val roundupService = RoundupServiceImpl(client)

        val roundUp = roundupService.getRoundupSumForTransactions(listOf())

        assertEquals(0, roundUp)
    }

    @Test
    fun `GivenARoundUpWhenItIsTransferred`() {
        val roundupService = RoundupServiceImpl(client)

        val outgoingTransactions = roundupService.getAllOutgoingTransactionsForFirstAccount()

        val roundUp = roundupService.getRoundupSumForTransactions(outgoingTransactions.second)

        val info = roundupService.createAndTransferToSavingsGoal(accountUid, "Investment", 1000, roundUp)

        assertEquals(info.savedPercentage, 12)
        verify(client).createSavingsGoal(accountUid, "Investment", 1000)
        verify(client).transferToSavingsGoal(any(), any(), any(), any())
    }

    @Test
    fun `GivenNonRoundUpWhenItIsTransferred`() {
        val roundupService = RoundupServiceImpl(client)
        val roundUp = roundupService.getRoundupSumForTransactions(listOf())

        val info = roundupService.createAndTransferToSavingsGoal(accountUid, "Investment", 1000, roundUp)

        assertEquals(info.savedPercentage, 12)
        verify(client, times(1)).createSavingsGoal(accountUid, "Investment", 1000)
    }
}