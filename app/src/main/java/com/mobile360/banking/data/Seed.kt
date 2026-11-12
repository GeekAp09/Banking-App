package com.mobile360.banking.data

import android.os.Build
import com.mobile360.banking.BuildConfig

object DemoAccounts {
    const val CUSTOMER_PHONE = "+1 555 010 4521"
    const val WEALTH_MANAGER_PHONE = "+1 555 010 7788"
    const val DEMO_CODE = "123456"
    const val ROLE_CUSTOMER = "CUSTOMER"
    const val ROLE_WEALTH_MANAGER = "WEALTH_MANAGER"
}

object SupportInfo {
    const val PHONE = "1-800-555-0142"
}

object SeedData {
    fun contacts() = listOf(
        ContactEntity("ct-1", "Daniel Okafor", "+1 (555) 234-8901", true),
        ContactEntity("ct-2", "Priya Shah", "+1 (555) 876-5432", false),
        ContactEntity("ct-3", "Maria Lopez", "+1 (555) 432-1098", false)
    )
}

/** Seeds the same sample records for the demo accounts and for any new account. */
suspend fun BankDao.seedIfEmpty() {
    if (userCount() > 0) return
    val now = System.currentTimeMillis()

    insertAccounts(listOf(
        AccountEntity("a1", "Everyday Savings", "SAVINGS", 1_248_025, "Active", now),
        AccountEntity("a2", "Primary Checking", "CHECKING", 420_010, "Active", now),
        AccountEntity("a3", "Bills Checking", "CHECKING", 275_005, "Active", now)
    ))
    insertCards(listOf(
        CardEntity("c1", "Visa", "4821", 500_000, 64_010, 62_510, Times.futureDayAt(14))
    ))
    insertTransactions(listOf(
        TransactionEntity("t1", "c1", "Whole Foods", "Groceries", -5_420, Times.dayAt(0, 8)),
        TransactionEntity("t2", "a1", "Salary", "Income", 320_000, Times.dayAt(-1, 9)),
        TransactionEntity("t3", "c1", "Uber", "Transport", -1_875, Times.dayAt(-3, 18)),
        TransactionEntity("t4", "c1", "Blue Bottle", "Dining", -650, Times.dayAt(0, 7)),
        TransactionEntity("t5", "c1", "Shell", "Transport", -4_200, Times.dayAt(-1, 17))
    ))
    insertHoldings(listOf(
        HoldingEntity("h1", "Global Index Fund", "Fund", 1_440_000),
        HoldingEntity("h2", "Apple Inc.", "Share", 960_000),
        HoldingEntity("h3", "Treasury Bond 2030", "Bond", 480_000)
    ))
    insertLinked(listOf(LinkedAccountEntity("l1", "Northern Bank Current", "•••• 4892", true)))
    insertContacts(SeedData.contacts())
    insertVaultDocs(listOf(
        VaultDocEntity("v1", "Tax return 2025.pdf", "Secure PDF", "2.4 MB", Times.dayAt(-30)),
        VaultDocEntity("v2", "Passport scan.jpg", "Identity Verified", "4.1 MB", Times.dayAt(-60)),
        VaultDocEntity("v3", "Mortgage statement.pdf", "Bank Verified", "1.8 MB", Times.dayAt(-45))
    ))
    insertDevices(listOf(
        DeviceEntity("d1", Build.MODEL, true, now, "Banking App v${BuildConfig.VERSION_NAME}", false),
        DeviceEntity("d2", "Galaxy S21", false, Times.dayAt(-1, 20), "Banking App", false)
    ))
    insertConsents(listOf(
        ConsentEntity("consent-contacts", "Contacts sync", "Enables seamless transfers to recognized beneficiaries.", true),
        ConsentEntity("consent-external", "External accounts", "Read-only balance synchronization via Open Banking API.", true)
    ))
    insertRounds(listOf(
        RoundEventEntity("r1", "Round up processed", 50, "SAVED", "Saved", Times.dayAt(0, 8)),
        RoundEventEntity("r2", "Low funds threshold", 0, "SKIPPED", "Skipped", Times.dayAt(-1, 17))
    ))
    insertInsights(listOf(
        InsightEntity(
            "i1", "Weekly Insight", "Dining is up 18% this week",
            "You've spent \$142.50 across dining & cafes over the past 7 days, outpacing your weekly target.",
            14_250, 12_000, now - 2 * 3_600_000
        )
    ))
    insertFaqs(listOf(
        FaqEntity("f1", "FAQ", "Security", "How do I reset my PIN?", "Open More > Privacy, devices, closure, then choose Security settings and verify your identity to set a new PIN."),
        FaqEntity("f2", "FAQ", "Transfers", "Why was my transfer blocked?", "Transfers are blocked when they exceed your per-transfer or daily limit, or when funds are insufficient."),
        FaqEntity("f3", "FAQ", "Privacy", "Is my data safe?", "Your data is encrypted in transit and you can withdraw consents or request deletion at any time."),
        FaqEntity("f4", "FAQ", "Authentication", "How do I enable hardware biometrics?", "Enroll a fingerprint or face in your device settings, then choose biometric sign-in on the sign-in screen."),
        FaqEntity("f5", "COMPLIANCE", "Security and compliance", "256-bit AES Encrypted", "Bank-grade data encryption at rest and in transit."),
        FaqEntity("f6", "COMPLIANCE", "Security and compliance", "PCI-DSS Level 1 Certified", "Strict annual third-party audits of payment pathways."),
        FaqEntity("f7", "COMPLIANCE", "Security and compliance", "Local biometric & on-device key management", "Cryptographic tokens never leave your Secure Enclave."),
        FaqEntity("f8", "COMPLIANCE", "Security and compliance", "Regulated by FINRA & SIPC account protection.", "Institutional custodian standards.")
    ))
    insertNotifications(listOf(
        NotificationEntity("n1", "TRANSACTION", "Salary received", "+\$3,200.00 was deposited to Everyday Savings.", Times.dayAt(-1, 9), true),
        NotificationEntity("n2", "SAVING_ROUND", "Round up processed", "\$0.50 moved to Everyday Savings.", Times.dayAt(0, 8), false),
        NotificationEntity("n3", "FRAUD", "New device sign-in", "A sign-in from ${Build.MODEL} was verified.", Times.dayAt(0, 7), false)
    ))

    val clientAt = Times.dayAt(-40)
    insertClients(listOf(
        ClientEntity("c-grace", "Grace Mensah", "BK-849201", "Active Portfolio", "Mandate: Core", 21_000_000, 14.2,
            "Discretionary Mandate", "USD", "Balanced", clientAt,
            "Rebalance to maintain 55% equity target. Lock in quarterly gains from global equities into fixed income.",
            "NOT_AUTHORISED", 64.8, 65.0),
        ClientEntity("c-tom", "Tom Alvarez", "BK-702115", "Growth Strategy", "Risk: High", 9_650_000, 8.7,
            "Advisory Mandate", "USD", "Growth", Times.dayAt(-75), "Keep growth tilt; review technology weighting next quarter.",
            "AUTHORISED", 70.0, 80.0),
        ClientEntity("c-nina", "Nina Rossi", "BK-655340", "Balanced Strategy", "Annual Review", 15_430_000, 5.4,
            "Advisory Mandate", "USD", "Balanced", Times.dayAt(-120), "Annual review due; consider increasing bond ladder duration.",
            "AUTHORISED", 40.0, 55.0)
    ))
    insertClientHoldings(listOf(
        ClientHoldingEntity("ch1", "c-grace", "Global Equity Fund", "Core Growth", "GEF-ACC", 55, 11_550_000, 14_250),
        ClientHoldingEntity("ch2", "c-grace", "Bonds", "Sovereign & Corp", "BND-SCP", 30, 6_300_000, 10_100),
        ClientHoldingEntity("ch3", "c-grace", "Cash", "Yield Reserve", "CASH-USD", 15, 3_150_000, 100),
        ClientHoldingEntity("ch4", "c-tom", "Growth Equity Fund", "Technology & Innovation", "GRW-ACC", 70, 6_755_000, 21_800),
        ClientHoldingEntity("ch5", "c-tom", "Bonds", "Sovereign & Corp", "BND-SCP", 20, 1_930_000, 10_100),
        ClientHoldingEntity("ch6", "c-tom", "Cash", "Yield Reserve", "CASH-USD", 10, 965_000, 100),
        ClientHoldingEntity("ch7", "c-nina", "Balanced Fund", "Multi-asset", "BAL-ACC", 45, 6_943_500, 12_600),
        ClientHoldingEntity("ch8", "c-nina", "Bonds", "Sovereign & Corp", "BND-SCP", 40, 6_172_000, 10_100),
        ClientHoldingEntity("ch9", "c-nina", "Cash", "Yield Reserve", "CASH-USD", 15, 2_314_500, 100)
    ))

    // Users last so a half-finished seed is retried.
    insertUsers(listOf(
        UserEntity(DemoAccounts.CUSTOMER_PHONE, "Amara Eze", DemoAccounts.ROLE_CUSTOMER),
        UserEntity(DemoAccounts.WEALTH_MANAGER_PHONE, "Marcus Bell", DemoAccounts.ROLE_WEALTH_MANAGER)
    ))
}
