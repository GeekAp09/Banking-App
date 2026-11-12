package com.mobile360.banking

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobile360.banking.data.BankDatabase
import com.mobile360.banking.data.BankRepository
import com.mobile360.banking.data.RoomBankRepository
import com.mobile360.banking.data.SessionRepository
import com.mobile360.banking.data.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Manual dependency graph: repositories shared by every ViewModel. */
object AppGraph {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var seedJob: Job
    lateinit var bank: BankRepository
        private set
    lateinit var session: SessionRepository
        private set

    fun init(context: Context) {
        val app = context.applicationContext
        val db = BankDatabase.create(app)
        seedJob = scope.launch { db.dao().seedIfEmpty() }
        bank = RoomBankRepository(db.dao())
        session = SessionRepository(app, db.dao()) { seedJob.join() }
    }
}

/** Usage in a screen: viewModel(factory = vmFactory { MyViewModel(AppGraph.bank, AppGraph.session) }). */
inline fun <reified T : ViewModel> vmFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
    }
