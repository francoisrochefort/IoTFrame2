package com.etrak.scaleshutdown.di

import android.content.Context
import com.etrak.scaleshutdown.shutdown_service.ShutdownManager

class AppModuleImpl(context: Context) : AppModule {

    override val shutdownService by lazy {
        ShutdownManager(context)
    }
}