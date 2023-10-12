package com.etrak.scaleshutdown.di

import com.ecotrak.shutdown_service.ShutdownManager

interface AppModule {
    val shutdownService: ShutdownManager
}