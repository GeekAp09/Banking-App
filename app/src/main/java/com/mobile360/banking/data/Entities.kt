package com.mobile360.banking.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(@PrimaryKey val phone: String, val name: String, val role: String)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val balanceCents: Long,
    val status: String,
    val updatedAt: Long
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val last4: String,
    val limitCents: Long,
    val balanceCents: Long,
    val bankRecordCents: Long,
    val dueAt: Long
) {
    val maskedNumber get() = "•••• $last4"
    val availableCents get() = limitCents - balanceCents
    val hasDiscrepancy get() = balanceCents != bankRecordCents
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val title: String,
    val category: String,
    val amountCents: Long,
    val at: Long
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val assetClass: String,
    val valueCents: Long
)

@Entity(tableName = "linked_accounts")
data class LinkedAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mask: String,
    val verified: Boolean
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val instant: Boolean
) {
    val initials get() = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
}

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey val id: String,
    val recipientName: String,
    val recipientPhone: String,
    val amountCents: Long,
    val feeCents: Long,
    val fromAccountId: String,
    val status: String,
    val at: Long,
    val reference: String
)

@Entity(tableName = "vault_docs")
data class VaultDocEntity(
    @PrimaryKey val id: String,
    val name: String,
    val badge: String,
    val sizeLabel: String,
    val updatedAt: Long
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isCurrent: Boolean,
    val lastActiveAt: Long,
    val appInfo: String,
    val blocked: Boolean
)

@Entity(tableName = "consents")
data class ConsentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val granted: Boolean
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val accountNo: String,
    val strategy: String,
    val tag: String,
    val aumCents: Long,
    val ytdPct: Double,
    val mandate: String,
    val baseCurrency: String,
    val riskProfile: String,
    val assessedAt: Long,
    val adviceNote: String,
    val authStatus: String,
    val growthExposurePct: Double,
    val riskThresholdPct: Double
) {
    val initials get() = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
}

@Entity(tableName = "client_holdings")
data class ClientHoldingEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val name: String,
    val subtitle: String,
    val symbol: String,
    val allocationPct: Int,
    val valueCents: Long,
    val navCents: Long
)

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val side: String,
    val instrument: String,
    val quantity: Int,
    val estimatedCents: Long,
    val status: String,
    val at: Long
)

@Entity(tableName = "audit_events")
data class AuditEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val detail: String,
    val at: Long
)

@Entity(tableName = "round_events")
data class RoundEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountCents: Long,
    val status: String,
    val note: String,
    val at: Long
)

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey val id: String,
    val label: String,
    val title: String,
    val explanation: String,
    val spentCents: Long,
    val targetCents: Long,
    val updatedAt: Long
)

@Entity(tableName = "category_rules")
data class CategoryRuleEntity(@PrimaryKey val merchant: String, val category: String)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey val id: String,
    val group: String,
    val section: String,
    val question: String,
    val answer: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val at: Long,
    val read: Boolean
)

// CR-002: locally stored feedback / bug report
@Entity(tableName = "feedback_reports")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val message: String,
    val createdAt: Long,
)
