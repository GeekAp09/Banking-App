package com.mobile360.banking

import com.mobile360.banking.data.AppPrefs
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.ui.screens.PrivacyAndDevicesViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// CR-007: records what the view model asked the repositories to do.
private object Calls {
    val log = mutableListOf<String>()
    var eraseFails = false
}

// Built without running the constructor, so no Context or DataStore is needed.
private open class FakeSession : SessionRepository(
    context = throw IllegalStateException("never constructed"),
    dao = throw IllegalStateException("never constructed"),
) {
    override val prefs: Flow<AppPrefs>
        get() = flowOf(AppPrefs(false, false, false, false, false, "NONE"))

    override suspend fun eraseSession() {
        Calls.log += "eraseSession"
    }

    override suspend fun setClosureStatus(status: String) {
        Calls.log += "closure:$status"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class Cr007AccountErasureTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Calls.log.clear()
        Calls.eraseFails = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeBank(): BankRepository =
        Proxy.newProxyInstance(
            BankRepository::class.java.classLoader,
            arrayOf(BankRepository::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "eraseUserData" -> {
                    if (Calls.eraseFails) throw IllegalStateException("disk full")
                    Calls.log += "eraseUserData"
                    Unit
                }
                "audit" -> Unit
                "devices", "consents" -> flowOf(emptyList<Any>())
                else -> null
            }
        } as BankRepository

    private fun newViewModel(): PrivacyAndDevicesViewModel {
        val unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null) as sun.misc.Unsafe
        val session = unsafe.allocateInstance(FakeSession::class.java) as SessionRepository
        return PrivacyAndDevicesViewModel(fakeBank(), session)
    }

    @Test
    fun cr_007_wrong_code_does_not_open_confirm_or_erase() {
        val vm = newViewModel()
        vm.requestClosure("000000") {}
        assertFalse(vm.confirmEraseVisible.value)
        assertTrue(vm.closureError.value != null)
        assertFalse(Calls.log.contains("eraseUserData"))
    }

    @Test
    fun cr_007_correct_code_asks_for_confirmation_and_cancel_erases_nothing() {
        val vm = newViewModel()
        vm.requestClosure("123456") {}
        assertTrue(vm.confirmEraseVisible.value)
        vm.cancelErase()
        assertFalse(vm.confirmEraseVisible.value)
        assertFalse(Calls.log.contains("eraseUserData"))
        assertFalse(Calls.log.contains("eraseSession"))
    }

    @Test
    fun cr_007_confirm_erases_data_then_clears_session_then_signs_out() {
        val vm = newViewModel()
        var erased = false
        vm.requestClosure("123456") {}
        vm.confirmErase { erased = true }
        assertEquals(listOf("eraseUserData", "eraseSession"), Calls.log.filter { it.startsWith("erase") })
        assertTrue(erased)
        assertNull(vm.eraseError.value)
    }

    @Test
    fun cr_007_erase_failure_shows_message_and_keeps_session() {
        val vm = newViewModel()
        var erased = false
        Calls.eraseFails = true
        vm.requestClosure("123456") {}
        vm.confirmErase { erased = true }
        assertEquals("Could not delete the account: disk full.", vm.eraseError.value)
        assertFalse(erased)
        assertFalse(Calls.log.contains("eraseSession"))
        vm.clearEraseError()
        assertNull(vm.eraseError.value)
    }
}
