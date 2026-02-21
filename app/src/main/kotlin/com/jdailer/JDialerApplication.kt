package com.jdailer

import android.app.Application
import com.jdailer.core.di.appModules
import com.jdailer.core.di.coreModule
import com.jdailer.core.di.dataModule
import com.jdailer.core.di.domainModule
import com.jdailer.core.di.presentationModule
import com.jdailer.core.sync.CommunicationsSyncScheduler
import timber.log.Timber
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class JDialerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@JDialerApplication)
            modules(
                listOf(
                    coreModule,
                    dataModule,
                    domainModule,
                    appModules,
                    presentationModule
                )
            )
        }
        CommunicationsSyncScheduler.schedule(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
