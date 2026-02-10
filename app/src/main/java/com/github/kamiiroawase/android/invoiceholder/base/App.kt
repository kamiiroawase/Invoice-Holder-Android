package com.github.kamiiroawase.android.invoiceholder.base

import android.app.Application

class App : Application() {
    companion object {
        lateinit var INSTANCE: App private set
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
    }
}
