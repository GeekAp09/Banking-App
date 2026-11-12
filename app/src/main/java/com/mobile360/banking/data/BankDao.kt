package com.mobile360.banking.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT COUNT(*) FROM users") suspend fun userCount(): Int
    @Query("SELECT * FROM users WHERE phone = :phone") suspend fun user(phone: String): UserEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertUsers(v: List<UserEntity>)

    @Query("SELECT * FROM accounts ORDER BY id") fun accounts(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts WHERE id = :id") suspend fun account(id: String): AccountEntity?
    @Query("UPDATE accounts SET balanceCents = :cents, updatedAt = :at WHERE id = :id")
    suspend fun setBalance(id: String, cents: Long, at: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAccounts(v: List<AccountEntity>)

    @Query("SELECT * FROM cards ORDER BY id") fun cards(): Flow<List<CardEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCards(v: List<CardEntity>)

    @Query("SELECT * FROM transactions ORDER BY at DESC") fun transactions(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun transaction(id: String): TransactionEntity?
    @Query("UPDATE transactions SET category = :category WHERE id = :id") suspend fun setCategory(id: String, category: String)
    @Query("UPDATE transactions SET category = :category WHERE title = :title") suspend fun setCategoryForTitle(title: String, category: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransactions(v: List<TransactionEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransaction(v: TransactionEntity)

    @Query("SELECT * FROM category_rules") fun categoryRules(): Flow<List<CategoryRuleEntity>>
    @Query("SELECT * FROM category_rules WHERE merchant = :merchant") suspend fun rule(merchant: String): CategoryRuleEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRule(v: CategoryRuleEntity)

    @Query("SELECT * FROM holdings ORDER BY valueCents DESC") fun holdings(): Flow<List<HoldingEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHoldings(v: List<HoldingEntity>)

    @Query("SELECT * FROM linked_accounts ORDER BY id") fun linkedAccounts(): Flow<List<LinkedAccountEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLinked(v: List<LinkedAccountEntity>)
    @Query("DELETE FROM linked_accounts WHERE id = :id") suspend fun deleteLinked(id: String)
    @Query("DELETE FROM linked_accounts") suspend fun deleteAllLinked()

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :q || '%' OR phone LIKE '%' || :q || '%' ORDER BY name")
    fun contacts(q: String): Flow<List<ContactEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertContacts(v: List<ContactEntity>)
    @Query("DELETE FROM contacts") suspend fun deleteContacts()

    @Query("SELECT * FROM transfers ORDER BY at DESC") fun transfers(): Flow<List<TransferEntity>>
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transfers WHERE at >= :since AND status = 'COMPLETED'")
    suspend fun transferredSince(since: Long): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransfer(v: TransferEntity)

    @Query("SELECT * FROM vault_docs WHERE name LIKE '%' || :q || '%' ORDER BY updatedAt DESC")
    fun vaultDocs(q: String): Flow<List<VaultDocEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertVaultDocs(v: List<VaultDocEntity>)
    @Query("DELETE FROM vault_docs WHERE id = :id") suspend fun deleteVaultDoc(id: String)

    @Query("SELECT * FROM devices ORDER BY isCurrent DESC, lastActiveAt DESC") fun devices(): Flow<List<DeviceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertDevices(v: List<DeviceEntity>)
    @Query("UPDATE devices SET blocked = 1 WHERE id = :id AND isCurrent = 0") suspend fun blockDevice(id: String)
    @Query("UPDATE devices SET lastActiveAt = :at WHERE isCurrent = 1") suspend fun touchCurrentDevice(at: Long)

    @Query("SELECT * FROM consents ORDER BY id") fun consents(): Flow<List<ConsentEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertConsents(v: List<ConsentEntity>)
    @Query("UPDATE consents SET granted = :granted WHERE id = :id") suspend fun setConsent(id: String, granted: Boolean)

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :q || '%' OR accountNo LIKE '%' || :q || '%' ORDER BY aumCents DESC")
    fun clients(q: String): Flow<List<ClientEntity>>
    @Query("SELECT * FROM clients WHERE id = :id") fun client(id: String): Flow<ClientEntity?>
    @Query("SELECT * FROM clients WHERE id = :id") suspend fun clientOnce(id: String): ClientEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertClients(v: List<ClientEntity>)
    @Query("UPDATE clients SET riskProfile = :risk, adviceNote = :note, assessedAt = :at WHERE id = :id")
    suspend fun saveAdvice(id: String, risk: String, note: String, at: Long)
    @Query("UPDATE clients SET authStatus = :status WHERE id = :id") suspend fun setAuthStatus(id: String, status: String)

    @Query("SELECT * FROM client_holdings WHERE clientId = :clientId ORDER BY valueCents DESC")
    fun clientHoldings(clientId: String): Flow<List<ClientHoldingEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertClientHoldings(v: List<ClientHoldingEntity>)
    @Query("SELECT * FROM client_holdings WHERE clientId = :clientId ORDER BY valueCents DESC LIMIT 1")
    suspend fun topClientHolding(clientId: String): ClientHoldingEntity?

    @Query("SELECT * FROM trades ORDER BY at DESC") fun trades(): Flow<List<TradeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTrade(v: TradeEntity)

    @Insert suspend fun insertAudit(v: AuditEventEntity)
    @Query("SELECT * FROM audit_events ORDER BY at DESC") fun audit(): Flow<List<AuditEventEntity>>

    @Query("SELECT * FROM round_events ORDER BY at DESC") fun rounds(): Flow<List<RoundEventEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRounds(v: List<RoundEventEntity>)

    @Query("SELECT * FROM insights ORDER BY updatedAt DESC") fun insights(): Flow<List<InsightEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertInsights(v: List<InsightEntity>)

    @Query("SELECT * FROM faqs WHERE question LIKE '%' || :q || '%' OR answer LIKE '%' || :q || '%' OR section LIKE '%' || :q || '%' ORDER BY id")
    fun faqs(q: String): Flow<List<FaqEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertFaqs(v: List<FaqEntity>)

    @Query("SELECT * FROM notifications ORDER BY at DESC") fun notifications(): Flow<List<NotificationEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNotifications(v: List<NotificationEntity>)
}
