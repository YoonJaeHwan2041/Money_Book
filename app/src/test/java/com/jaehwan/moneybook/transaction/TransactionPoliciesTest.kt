package com.jaehwan.moneybook.transaction

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.splitmember.domain.computeSuggestedShares
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import com.jaehwan.moneybook.transaction.ui.LedgerRow
import com.jaehwan.moneybook.transaction.ui.MonthlyTotals
import com.jaehwan.moneybook.transaction.ui.SplitDraftValidationInput
import com.jaehwan.moneybook.transaction.ui.SplitMemberDraftInput
import com.jaehwan.moneybook.transaction.ui.calculateCurrentBalance
import com.jaehwan.moneybook.transaction.ui.calculateMonthlyTotals
import com.jaehwan.moneybook.transaction.ui.validateSplitInput
import com.jaehwan.moneybook.transaction.ui.validateTransactionAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TransactionPoliciesTest {

    @Test
    fun moneyInput_formatAndParse_works() {
        val formatted = com.jaehwan.moneybook.transaction.domain.formatMoneyInput("0012300")
        val parsed = com.jaehwan.moneybook.transaction.domain.parseMoneyInput(formatted)
        assertEquals("12,300", formatted)
        assertEquals(12300, parsed)
    }

    @Test
    fun splitStatusPolicies_work() {
        val members = listOf(
            SplitMemberEntity(transactionId = 1, memberName = "나", isPrimaryPayer = true, agreedAmount = 10000, isPaid = true),
            SplitMemberEntity(transactionId = 1, memberName = "A", agreedAmount = 20000, isPaid = true),
            SplitMemberEntity(transactionId = 1, memberName = "B", agreedAmount = 30000, isPaid = false),
        )
        assertFalse(com.jaehwan.moneybook.transaction.domain.isSplitComplete(members))
        assertEquals(30000, com.jaehwan.moneybook.transaction.domain.unpaidTotal(members))
    }

    @Test
    fun monthlyAndCurrentBalanceCalculations_work() {
        val zone = ZoneId.systemDefault()
        val d = LocalDate.of(2026, 4, 10).atStartOfDay(zone).toInstant().toEpochMilli()
        val rows = listOf(
            row(amount = 100000, type = TransactionType.INCOME.key, expectedDate = d),
            row(amount = 40000, type = TransactionType.EXPENSE.key, expectedDate = d),
            row(amount = 20000, type = TransactionType.FIXED_EXPENSE.key, isConfirmed = false, expectedDate = d),
        )
        val monthly = calculateMonthlyTotals(rows)
        assertEquals(MonthlyTotals(income = 100000, expense = 40000), monthly)
        assertEquals(60000, calculateCurrentBalance(rows))
    }

    @Test
    fun validationPolicies_work() {
        assertEquals("금액 칸은 빈칸입니다.", validateTransactionAmount(""))
        assertNull(validateTransactionAmount("1,000"))

        val invalid = validateSplitInput(
            SplitDraftValidationInput(
                participantCountText = "3",
                selfAmountText = "1000",
                memberDrafts = listOf(
                    SplitMemberDraftInput(name = "A", amountText = "500"),
                    SplitMemberDraftInput(name = "", amountText = "500"),
                ),
            )
        )
        assertTrue(invalid?.contains("멤버 2 이름") == true)
    }

    @Test
    fun computeSuggestedShares_keepsTotal() {
        val shares = computeSuggestedShares(
            total = 100000,
            count = 3,
            extras = listOf(0, 1000, 0),
            deductions = listOf(0, 0, 500),
            primaryIndex = 0,
        )
        assertEquals(100000, shares.sum())
    }

    private fun row(
        amount: Int,
        type: String,
        expectedDate: Long,
        isConfirmed: Boolean = true,
    ): LedgerRow {
        return LedgerRow(
            transaction = TransactionEntity(
                categoryId = 1L,
                amount = amount,
                type = type,
                isConfirmed = isConfirmed,
                expectedDate = expectedDate,
                hasAlarm = false,
            ),
            categoryName = "테스트",
        )
    }
}
