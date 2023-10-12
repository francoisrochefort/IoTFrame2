package com.etrak.scaleshutdown

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.etrak.scaleshutdown.di.AppModule
import com.etrak.scaleshutdown.di.AppModuleImpl
import com.etrak.scaleshutdown.shutdown_service.ShutdownService.Companion.CHANNEL_ID

class ScaleApp : Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}