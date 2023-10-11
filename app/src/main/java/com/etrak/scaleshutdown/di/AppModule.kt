package com.etrak.scaleshutdown.di

import com.etrak.scaleshutdown.shutdown_service.ShutdownManager

interface AppModule {
    val shutdownService: ShutdownManager
}