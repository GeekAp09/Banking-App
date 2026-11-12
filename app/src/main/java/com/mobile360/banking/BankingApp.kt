package com.mobile360.banking

import android.app.Application

class BankingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
