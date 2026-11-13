package com.mobile360.banking.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// CR-002: FeedbackEntity added; the version is bumped (destructive fallback reseeds demo data).
@Database(
    entities = [
        UserEntity::class, AccountEntity::class, CardEntity::class, TransactionEntity::class,
        HoldingEntity::class, LinkedAccountEntity::class, ContactEntity::class, TransferEntity::class,
        VaultDocEntity::class, DeviceEntity::class, ConsentEntity::class, ClientEntity::class,
        ClientHoldingEntity::class, TradeEntity::class, AuditEventEntity::class, RoundEventEntity::class,
        InsightEntity::class, CategoryRuleEntity::class, FaqEntity::class, NotificationEntity::class,
        FeedbackEntity::class,
    ],
    version = 100,
    exportSchema = false,
)
abstract class BankDatabase : RoomDatabase() {
    abstract fun dao(): BankDao

    companion object {
        fun create(context: Context): BankDatabase =
            Room.databaseBuilder(context, BankDatabase::class.java, "banking.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
