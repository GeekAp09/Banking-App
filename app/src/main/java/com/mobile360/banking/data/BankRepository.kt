package com.mobile360.banking.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class AssetSlice(val label: String, val cents: Long, val percent: Int)

sealed interface TransferResult {
    data class Success(val transfer: TransferEntity) : TransferResult
    data object InsufficientFunds : TransferResult
    data class OverPerTransferLimit(val limitCents: Long) : TransferResult
    data class OverDailyLimit(val limitCents: Long) : TransferResult
    data object Invalid : TransferResult
}

data class TradePreview(
    val estimatedCents: Long,
    val feeCents: Long,
    val postWeightPct: Double,
    val newExposurePct: Double,
    val thresholdPct: Double,
    val exceedsRisk: Boolean,
    val blockedReason: String?
)

sealed interface TradeResult {
    data class Success(val trade: TradeEntity) : TradeResult
    data class Blocked(val reason: String) : TradeResult
    data object NeedsReverification : TradeResult
    data object Invalid : TradeResult
}

object Limits {
    const val PER_TRANSFER_CENTS = 200_000L
    const val DAILY_CENTS = 500_000L
    const val FEE_RATE = 0.0015
}

interface BankRepository {
    fun accounts(): Flow<List<AccountEntity>>
    fun cards(): Flow<List<CardEntity>>
    fun transactions(): Flow<List<TransactionEntity>>
    fun holdings(): Flow<List<HoldingEntity>>
    fun assetMix(): Flow<List<AssetSlice>>
    fun totalWealthCents(): Flow<Long>
    fun linkedAccounts(): Flow<List<LinkedAccountEntity>>
    suspend fun linkAccount(name: String, mask: String, consent: Boolean): Boolean
    suspend fun removeLinkedAccount(id: String)

    fun insights(): Flow<List<InsightEntity>>
    fun rounds(): Flow<List<RoundEventEntity>>
    suspend fun changeCategory(transactionId: String, category: String)
    suspend fun applyRoundUp(transactionId: String): RoundEventEntity?

    fun contacts(query: String): Flow<List<ContactEntity>>
    fun transfers(): Flow<List<TransferEntity>>
    suspend fun sendTransfer(recipientName: String, recipientPhone: String, amountCents: Long): TransferResult

    fun vaultDocs(query: String): Flow<List<VaultDocEntity>>
    suspend fun addVaultDoc(name: String, sizeLabel: String)
    suspend fun deleteVaultDoc(id: String)
    suspend fun logVaultAccess(id: String)

    fun devices(): Flow<List<DeviceEntity>>
    suspend fun blockDevice(id: String)
    fun consents(): Flow<List<ConsentEntity>>
    suspend fun setConsent(id: String, granted: Boolean)
    fun faqs(query: String): Flow<List<FaqEntity>>
    fun notifications(): Flow<List<NotificationEntity>>

    fun clients(query: String): Flow<List<ClientEntity>>
    fun client(id: String): Flow<ClientEntity?>
    fun clientHoldings(clientId: String): Flow<List<ClientHoldingEntity>>
    suspend fun saveAdvice(clientId: String, riskProfile: String, note: String)
    suspend fun requestTradeAuthorization(clientId: String)
    suspend fun previewTrade(clientId: String, quantity: Int): TradePreview?
    suspend fun submitTrade(clientId: String, side: String, quantity: Int, reverified: Boolean): TradeResult
    fun trades(): Flow<List<TradeEntity>>

    suspend fun audit(type: String, detail: String)

    suspend fun eraseUserData()
}

class RoomBankRepository(private val dao: BankDao) : BankRepository {
    override fun accounts() = dao.accounts()
    override fun cards() = dao.cards()
    override fun transactions() = dao.transactions()
    override fun holdings() = dao.holdings()

    override fun totalWealthCents() = combine(dao.accounts(), dao.holdings()) { a, h ->
        a.sumOf { it.balanceCents } + h.sumOf { it.valueCents }
    }

    override fun assetMix() = combine(dao.accounts(), dao.holdings()) { a, h ->
        val cash = a.sumOf { it.balanceCents }
        val total = (cash + h.sumOf { it.valueCents }).coerceAtLeast(1)
        fun pct(c: Long) = Math.round(c * 100.0 / total).toInt()
        val grouped = h.groupBy { it.assetClass }
        listOf("Cash" to cash, "Funds" to (grouped["Fund"]?.sumOf { it.valueCents } ?: 0L),
            "Shares" to (grouped["Share"]?.sumOf { it.valueCents } ?: 0L),
            "Bonds" to (grouped["Bond"]?.sumOf { it.valueCents } ?: 0L))
            .map { AssetSlice(it.first, it.second, pct(it.second)) }
    }

    override fun linkedAccounts() = dao.linkedAccounts()
    override suspend fun linkAccount(name: String, mask: String, consent: Boolean): Boolean {
        if (!consent) return false
        dao.insertLinked(listOf(LinkedAccountEntity("l-" + System.nanoTime(), name, mask, true)))
        audit("LINK_ACCOUNT", name)
        return true
    }

    override suspend fun removeLinkedAccount(id: String) {
        dao.deleteLinked(id)
        audit("UNLINK_ACCOUNT", id)
    }

    override fun insights() = dao.insights()
    override fun rounds() = dao.rounds()

    override suspend fun changeCategory(transactionId: String, category: String) {
        val tx = dao.transaction(transactionId) ?: return
        dao.insertRule(CategoryRuleEntity(tx.title, category))
        dao.setCategoryForTitle(tx.title, category)
    }

    override suspend fun applyRoundUp(transactionId: String): RoundEventEntity? {
        val tx = dao.transaction(transactionId) ?: return null
        val spend = -tx.amountCents
        if (spend <= 0) return null
        val diff = (100 - spend % 100) % 100
        if (diff == 0L) return null
        val from = dao.account("a2") ?: return null
        val to = dao.account("a1") ?: return null
        val now = System.currentTimeMillis()
        val ok = from.balanceCents - diff >= 10_000
        val event = RoundEventEntity(
            "r-" + System.nanoTime(), if (ok) "Round up processed" else "Low funds threshold",
            if (ok) diff else 0, if (ok) "SAVED" else "SKIPPED", if (ok) "Saved" else "Skipped", now
        )
        if (ok) {
            dao.setBalance(from.id, from.balanceCents - diff, now)
            dao.setBalance(to.id, to.balanceCents + diff, now)
        }
        dao.insertRounds(listOf(event))
        return event
    }

    override fun contacts(query: String) = dao.contacts(query.trim())
    override fun transfers() = dao.transfers()

    override suspend fun sendTransfer(recipientName: String, recipientPhone: String, amountCents: Long): TransferResult {
        if (amountCents <= 0) return TransferResult.Invalid
        if (amountCents > Limits.PER_TRANSFER_CENTS) return TransferResult.OverPerTransferLimit(Limits.PER_TRANSFER_CENTS)
        val dayStart = Times.dayAt(0, 0)
        if (dao.transferredSince(dayStart) + amountCents > Limits.DAILY_CENTS) return TransferResult.OverDailyLimit(Limits.DAILY_CENTS)
        val from = dao.account("a1") ?: return TransferResult.Invalid
        if (from.balanceCents < amountCents) return TransferResult.InsufficientFunds
        val now = System.currentTimeMillis()
        val transfer = TransferEntity(
            "tr-" + System.nanoTime(), recipientName, recipientPhone, amountCents, 0, from.id, "COMPLETED", now,
            "BK-${(1000..9999).random()}-TX${(100..999).random()}"
        )
        dao.setBalance(from.id, from.balanceCents - amountCents, now)
        dao.insertTransaction(TransactionEntity("tx-" + transfer.id, from.id, "Transfer to $recipientName", "Transfer", -amountCents, now))
        dao.insertTransfer(transfer)
        audit("TRANSFER", "${transfer.reference} to $recipientName")
        return TransferResult.Success(transfer)
    }

    override fun vaultDocs(query: String) = dao.vaultDocs(query.trim())
    override suspend fun addVaultDoc(name: String, sizeLabel: String) {
        dao.insertVaultDocs(listOf(VaultDocEntity("v-" + System.nanoTime(), name, "Secure PDF", sizeLabel, System.currentTimeMillis())))
        audit("VAULT_UPLOAD", name)
    }

    override suspend fun deleteVaultDoc(id: String) {
        dao.deleteVaultDoc(id)
        audit("VAULT_DELETE", id)
    }

    override suspend fun logVaultAccess(id: String) = audit("VAULT_ACCESS", id)

    override fun devices() = dao.devices()
    override suspend fun blockDevice(id: String) {
        dao.blockDevice(id)
        audit("BLOCK_DEVICE", id)
    }

    override fun consents() = dao.consents()
    override suspend fun setConsent(id: String, granted: Boolean) {
        dao.setConsent(id, granted)
        when {
            id == "consent-contacts" && !granted -> dao.deleteContacts()
            id == "consent-contacts" && granted -> dao.insertContacts(SeedData.contacts())
            id == "consent-external" && !granted -> dao.deleteAllLinked()
        }
        audit(if (granted) "CONSENT_GRANTED" else "CONSENT_WITHDRAWN", id)
    }

    override fun faqs(query: String) = dao.faqs(query.trim())
    override fun notifications() = dao.notifications()

    override fun clients(query: String) = dao.clients(query.trim())
    override fun client(id: String) = dao.client(id)
    override fun clientHoldings(clientId: String) = dao.clientHoldings(clientId)
    override fun trades() = dao.trades()

    override suspend fun saveAdvice(clientId: String, riskProfile: String, note: String) {
        dao.saveAdvice(clientId, riskProfile, note, System.currentTimeMillis())
        audit("ADVICE_SAVED", "$clientId $riskProfile")
    }

    override suspend fun requestTradeAuthorization(clientId: String) {
        dao.setAuthStatus(clientId, "PROMPT_SENT")
        audit("TRADE_AUTH_REQUESTED", clientId)
    }

    override suspend fun previewTrade(clientId: String, quantity: Int): TradePreview? {
        val c = dao.clientOnce(clientId) ?: return null
        val h = dao.topClientHolding(clientId) ?: return null
        val est = quantity * h.navCents
        val impact = est * 100.0 / c.aumCents
        val exposure = c.growthExposurePct + impact
        val blocked = if (c.authStatus != "AUTHORISED") "Blocked: client has not authorised trades" else null
        return TradePreview(est, Math.round(est * Limits.FEE_RATE), h.allocationPct + impact, exposure, c.riskThresholdPct, exposure > c.riskThresholdPct, blocked)
    }

    override suspend fun submitTrade(clientId: String, side: String, quantity: Int, reverified: Boolean): TradeResult {
        if (quantity <= 0) return TradeResult.Invalid
        val preview = previewTrade(clientId, quantity) ?: return TradeResult.Invalid
        preview.blockedReason?.let {
            audit("TRADE_BLOCKED", clientId)
            return TradeResult.Blocked(it)
        }
        if (!reverified) return TradeResult.NeedsReverification
        val h = dao.topClientHolding(clientId) ?: return TradeResult.Invalid
        val trade = TradeEntity("trd-" + System.nanoTime(), clientId, side, h.name, quantity, preview.estimatedCents, "EXECUTED", System.currentTimeMillis())
        dao.insertTrade(trade)
        audit("TRADE", "$side $quantity ${h.symbol} for $clientId")
        return TradeResult.Success(trade)
    }

    override suspend fun audit(type: String, detail: String) {
        dao.insertAudit(AuditEventEntity(type = type, detail = detail, at = System.currentTimeMillis()))
    }

    // CR-007: permanent erasure of personal data; audit_events are kept (retained by law)
    override suspend fun eraseUserData() {
        dao.deleteAccounts()
        dao.deleteCards()
        dao.deleteTransactions()
        dao.deleteTransfers()
        dao.deleteHoldings()
        dao.deleteConsents()
        dao.deleteDevices()
        dao.deleteVaultDocs()
        dao.deleteRounds()
        dao.deleteInsights()
        dao.deleteRules()
        dao.deleteNotifications()
        dao.deleteContacts()
        dao.deleteAllLinked()
        dao.deleteUsers()
        audit("ACCOUNT_ERASED", "personal data erased")
    }
}
