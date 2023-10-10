package com.etrak.scaleshutdown.di

import com.etrak.scaleshutdown.shutdown_service.ShutdownServiceFacade

interface AppModule {
    val shutdownService: ShutdownServiceFacade
}