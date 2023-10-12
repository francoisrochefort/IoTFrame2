package com.etrak.scaleshutdown

import android.app.Application
import com.etrak.scaleshutdown.di.AppModule
import com.etrak.scaleshutdown.di.AppModuleImpl

class ScaleApp : Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}