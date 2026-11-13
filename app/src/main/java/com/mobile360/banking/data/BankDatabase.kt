package com.mobile360.banking.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// CR-002: FeedbackEntity added; version 2 with MIGRATION_1_2 keeps existing data.
@Database(
    entities = [
        UserEntity::class, AccountEntity::class, CardEntity::class, TransactionEntity::class,
        HoldingEntity::class, LinkedAccountEntity::class, ContactEntity::class, TransferEntity::class,
        VaultDocEntity::class, DeviceEntity::class, ConsentEntity::class, ClientEntity::class,
        ClientHoldingEntity::class, TradeEntity::class, AuditEventEntity::class, RoundEventEntity::class,
        InsightEntity::class, CategoryRuleEntity::class, FaqEntity::class, NotificationEntity::class,
        FeedbackEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class BankDatabase : RoomDatabase() {
    abstract fun dao(): BankDao

    companion object {
        fun create(context: Context): BankDatabase =
            Room.databaseBuilder(context, BankDatabase::class.java, "banking.db")
                .addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration()
                .build()
    }
}

/** CR-002: adds the feedback_reports table and keeps existing data. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feedback_reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`type` TEXT NOT NULL, `message` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
    }
}
