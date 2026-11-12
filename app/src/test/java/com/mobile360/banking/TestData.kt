package com.mobile360.banking

import com.mobile360.banking.data.CardEntity
import com.mobile360.banking.data.TransactionEntity

// Shared test data for the BOLT-01 story tests.
object TestData {
    fun card(balance: Long = 100_000L, bankRecord: Long = 100_000L, limit: Long = 500_000L) = CardEntity(
        id = "c1", name = "Visa", last4 = "4821", limitCents = limit,
        balanceCents = balance, bankRecordCents = bankRecord, dueAt = 0L
    )

    fun tx(id: String, title: String, category: String, amount: Long = -500L) = TransactionEntity(
        id = id, accountId = "a2", title = title, category = category, amountCents = amount, at = 0L
    )
}
