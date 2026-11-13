package com.mobile360.banking.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore("session")

data class SessionState(
    val signedIn: Boolean = false,
    val phone: String = "",
    val name: String = "",
    val role: String = DemoAccounts.ROLE_CUSTOMER,
    val lastSignInAt: Long = 0L
) {
    val isWealthManager get() = role == DemoAccounts.ROLE_WEALTH_MANAGER
    val firstName get() = name.substringBefore(" ")
}

data class AppPrefs(
    val easyMode: Boolean = false,
    val roundUps: Boolean = true,
    val transactionAlerts: Boolean = true,
    val fraudAlerts: Boolean = true,
    val roundAlerts: Boolean = true,
    val closureStatus: String = "NONE"
)

enum class PrefFlag(val key: String) {
    EASY_MODE("easy_mode"), ROUND_UPS("round_ups"), TRANSACTION_ALERTS("alert_tx"),
    FRAUD_ALERTS("alert_fraud"), ROUND_ALERTS("alert_round")
}

sealed interface VerifyResult {
    data object Success : VerifyResult
    data class Wrong(val attemptsLeft: Int) : VerifyResult
    data class Locked(val untilMillis: Long) : VerifyResult
    data object Expired : VerifyResult
}

/** Local demo sign-in: no SMS or email is sent; the code is DemoAccounts.DEMO_CODE. */
class SessionRepository(
    private val context: Context,
    private val dao: BankDao,
    private val ready: suspend () -> Unit
) {
    private val store get() = context.sessionStore
    private object K {
        val signedIn = booleanPreferencesKey("signed_in")
        val mfaDone = booleanPreferencesKey("mfa_done")
        val phone = stringPreferencesKey("phone")
        val name = stringPreferencesKey("name")
        val role = stringPreferencesKey("role")
        val lastSignIn = longPreferencesKey("last_sign_in")
        val codeIssued = longPreferencesKey("code_issued")
        val fails = intPreferencesKey("fails")
        val lockedUntil = longPreferencesKey("locked_until")
        val closure = stringPreferencesKey("closure")
        val contactSync = booleanPreferencesKey("contact_sync")

        val lastSeenVersion = longPreferencesKey("last_seen_version")
    }

    val session: Flow<SessionState> = store.data.map { p ->
        SessionState(
            signedIn = (p[K.signedIn] ?: false) && (p[K.mfaDone] ?: false),
            phone = p[K.phone] ?: "",
            name = p[K.name] ?: "",
            role = p[K.role] ?: DemoAccounts.ROLE_CUSTOMER,
            lastSignInAt = p[K.lastSignIn] ?: 0L
        )
    }

    val prefs: Flow<AppPrefs> = store.data.map { p ->
        AppPrefs(
            easyMode = p[booleanPreferencesKey(PrefFlag.EASY_MODE.key)] ?: false,
            roundUps = p[booleanPreferencesKey(PrefFlag.ROUND_UPS.key)] ?: true,
            transactionAlerts = p[booleanPreferencesKey(PrefFlag.TRANSACTION_ALERTS.key)] ?: true,
            fraudAlerts = p[booleanPreferencesKey(PrefFlag.FRAUD_ALERTS.key)] ?: true,
            roundAlerts = p[booleanPreferencesKey(PrefFlag.ROUND_ALERTS.key)] ?: true,
            closureStatus = p[K.closure] ?: "NONE"
        )
    }

    val contactSync: Flow<Boolean> = store.data.map { it[K.contactSync] ?: false }

    suspend fun setContactSync(on: Boolean) {
        store.edit { it[K.contactSync] = on }
    }

    suspend fun startRoute(): String {
        val s = session.first()
        return when {
            !s.signedIn -> com.mobile360.banking.navigation.Routes.SIGN_IN
            s.isWealthManager -> com.mobile360.banking.navigation.Routes.CLIENT_LIST
            else -> com.mobile360.banking.navigation.Routes.HOME
        }
    }

    /** First factor passed (biometric or PIN): a code is issued for the customer demo account. */
    suspend fun beginMfa(phone: String = DemoAccounts.CUSTOMER_PHONE) {
        store.edit {
            it[K.phone] = phone
            it[K.signedIn] = false
            it[K.mfaDone] = false
            it[K.codeIssued] = System.currentTimeMillis()
            it[K.fails] = 0
        }
    }

    suspend fun resendCode() {
        store.edit { it[K.codeIssued] = System.currentTimeMillis(); it[K.fails] = 0 }
    }

    suspend fun codeIssuedAt(): Long = store.data.first()[K.codeIssued] ?: System.currentTimeMillis()

    suspend fun verifyCode(code: String): VerifyResult {
        val p = store.data.first()
        val now = System.currentTimeMillis()
        val locked = p[K.lockedUntil] ?: 0L
        if (locked > now) return VerifyResult.Locked(locked)
        if (now - (p[K.codeIssued] ?: 0L) > 5 * 60_000L) return VerifyResult.Expired
        if (code == DemoAccounts.DEMO_CODE) {
            completeSignIn(p[K.phone] ?: DemoAccounts.CUSTOMER_PHONE)
            return VerifyResult.Success
        }
        val fails = (p[K.fails] ?: 0) + 1
        return if (fails >= 3) {
            val until = now + 15 * 60_000L
            store.edit { it[K.fails] = 0; it[K.lockedUntil] = until }
            VerifyResult.Locked(until)
        } else {
            store.edit { it[K.fails] = fails }
            VerifyResult.Wrong(3 - fails)
        }
    }

    /** Demo shortcut offered by the verify screen: sign in as the seeded Wealth Manager account. */
    suspend fun continueAsWealthManager() = completeSignIn(DemoAccounts.WEALTH_MANAGER_PHONE)

    private suspend fun completeSignIn(phone: String) {
        ready()
        val user = dao.user(phone)
        val now = System.currentTimeMillis()
        dao.touchCurrentDevice(now)
        store.edit {
            it[K.phone] = phone
            it[K.name] = user?.name ?: ""
            it[K.role] = user?.role ?: DemoAccounts.ROLE_CUSTOMER
            it[K.signedIn] = true
            it[K.mfaDone] = true
            it[K.lastSignIn] = now
        }
    }

    suspend fun signOut() {
        store.edit { it[K.signedIn] = false; it[K.mfaDone] = false }
    }

    suspend fun setFlag(flag: PrefFlag, value: Boolean) {
        store.edit { it[booleanPreferencesKey(flag.key)] = value }
    }

    suspend fun setClosureStatus(status: String) {
        store.edit { it[K.closure] = status }
    }

    // CR-013: last app version code whose changelog the user has seen
    val lastSeenVersion: Flow<Long> get() = store.data.map { it[K.lastSeenVersion] ?: 0L }

    suspend fun markVersionSeen(code: Long) {
        store.edit { it[K.lastSeenVersion] = code }
    }
}
