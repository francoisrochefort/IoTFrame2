package com.etrak.scaleshutdown.di

import android.content.Context
import com.etrak.scaleshutdown.shutdown_service.ShutdownServiceFacade

class AppModuleImpl(context: Context) : AppModule {

    override val shutdownService by lazy {
        ShutdownServiceFacade(context)
    }
}