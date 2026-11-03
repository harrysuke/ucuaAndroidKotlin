package com.ucua.app

import android.app.Application
import com.ucua.app.core.di.AppContainer

class UcuaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
