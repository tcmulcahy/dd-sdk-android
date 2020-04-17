/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.rum.internal

import android.content.Context
import com.datadog.android.DatadogConfig
import com.datadog.android.DatadogEndpoint
import com.datadog.android.core.internal.CoreFeature
import com.datadog.android.core.internal.data.upload.DataUploadScheduler
import com.datadog.android.core.internal.data.upload.NoOpDataUploadScheduler
import com.datadog.android.core.internal.data.upload.UploadScheduler
import com.datadog.android.core.internal.domain.NoOpPersistenceStrategy
import com.datadog.android.core.internal.domain.PersistenceStrategy
import com.datadog.android.core.internal.net.NoOpDataUploader
import com.datadog.android.core.internal.net.info.NetworkInfoProvider
import com.datadog.android.core.internal.system.SystemInfoProvider
import com.datadog.android.log.internal.user.NoOpUserInfoProvider
import com.datadog.android.log.internal.user.UserInfoProvider
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.internal.domain.RumEvent
import com.datadog.android.rum.internal.domain.RumFileStrategy
import com.datadog.android.rum.internal.instrumentation.gestures.GesturesTracker
import com.datadog.android.rum.internal.instrumentation.gestures.NoOpGesturesTracker
import com.datadog.android.rum.internal.monitor.NoOpRumMonitor
import com.datadog.android.rum.internal.net.RumOkHttpUploader
import com.datadog.android.rum.internal.tracking.NoOpActionTrackingStrategy
import com.datadog.android.rum.internal.tracking.NoOpViewTrackingStrategy
import com.datadog.android.rum.internal.tracking.UserActionTrackingStrategy
import com.datadog.android.rum.internal.tracking.ViewTreeChangeTrackingStrategy
import com.datadog.android.rum.tracking.TrackingStrategy
import com.datadog.android.rum.tracking.ViewTrackingStrategy
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.OkHttpClient

internal object RumFeature {

    private val initialized = AtomicBoolean(false)

    internal var clientToken: String = ""
    internal var endpointUrl: String = DatadogEndpoint.RUM_US
    internal var serviceName: String = DatadogConfig.DEFAULT_SERVICE_NAME
    internal var envName: String = ""
    internal var applicationId: UUID = UUID(0, 0)

    internal var persistenceStrategy: PersistenceStrategy<RumEvent> = NoOpPersistenceStrategy()
    internal var uploader: com.datadog.android.core.internal.net.DataUploader = NoOpDataUploader()
    internal var dataUploadScheduler: UploadScheduler = NoOpDataUploadScheduler()
    internal var userInfoProvider: UserInfoProvider = NoOpUserInfoProvider()

    internal var gesturesTracker: GesturesTracker = NoOpGesturesTracker()
    internal var viewTrackingStrategy: ViewTrackingStrategy =
        NoOpViewTrackingStrategy()
    internal var actionTrackingStrategy: UserActionTrackingStrategy = NoOpActionTrackingStrategy()
    internal var viewTreeTrackingStrategy: TrackingStrategy =
        ViewTreeChangeTrackingStrategy()

    @Suppress("LongParameterList")
    fun initialize(
        appContext: Context,
        config: DatadogConfig.RumConfig,
        okHttpClient: OkHttpClient,
        networkInfoProvider: NetworkInfoProvider,
        systemInfoProvider: SystemInfoProvider,
        dataUploadThreadPoolExecutor: ScheduledThreadPoolExecutor,
        dataPersistenceExecutor: ExecutorService,
        userInfoProvider: UserInfoProvider
    ) {
        if (initialized.get()) {
            return
        }

        GlobalRum.updateApplicationId(config.applicationId)
        clientToken = config.clientToken
        endpointUrl = config.endpointUrl
        serviceName = config.serviceName
        envName = config.envName

        gesturesTracker = config.gesturesTracker ?: NoOpGesturesTracker()
        viewTrackingStrategy = config.viewTrackingStrategy ?: NoOpViewTrackingStrategy()
        actionTrackingStrategy = config.userActionTrackingStrategy ?: NoOpActionTrackingStrategy()

        persistenceStrategy = RumFileStrategy(
            appContext,
            dataPersistenceExecutorService = dataPersistenceExecutor
        )
        setupUploader(
            endpointUrl,
            okHttpClient,
            networkInfoProvider,
            systemInfoProvider,
            dataUploadThreadPoolExecutor = dataUploadThreadPoolExecutor
        )
        registerTrackingStrategies(appContext)
        this.userInfoProvider = userInfoProvider
        initialized.set(true)
    }

    fun isInitialized(): Boolean {
        return initialized.get()
    }

    fun stop() {
        if (initialized.get()) {
            dataUploadScheduler.stopScheduling()

            unregisterTrackingStrategies(CoreFeature.contextRef.get())

            persistenceStrategy = NoOpPersistenceStrategy()
            dataUploadScheduler = NoOpDataUploadScheduler()
            clientToken = ""
            endpointUrl = DatadogEndpoint.RUM_US
            serviceName = DatadogConfig.DEFAULT_SERVICE_NAME
            // reset rum monitor to NoOp and reset the flag
            GlobalRum.isRegistered.set(false)
            GlobalRum.registerIfAbsent(NoOpRumMonitor())
            GlobalRum.isRegistered.set(false)
            initialized.set(false)
        }
    }

    // region Internal

    private fun setupUploader(
        endpointUrl: String,
        okHttpClient: OkHttpClient,
        networkInfoProvider: NetworkInfoProvider,
        systemInfoProvider: SystemInfoProvider,
        dataUploadThreadPoolExecutor: ScheduledThreadPoolExecutor
    ) {
        uploader = RumOkHttpUploader(endpointUrl, clientToken, okHttpClient)

        dataUploadScheduler = DataUploadScheduler(
            persistenceStrategy.getReader(),
            uploader,
            networkInfoProvider,
            systemInfoProvider,
            dataUploadThreadPoolExecutor
        )
        dataUploadScheduler.startScheduling()
    }

    private fun registerTrackingStrategies(appContext: Context) {
        actionTrackingStrategy.register(appContext)
        viewTrackingStrategy.register(appContext)
        viewTreeTrackingStrategy.register(appContext)
    }

    private fun unregisterTrackingStrategies(appContext: Context?) {
        actionTrackingStrategy.unregister(appContext)
        viewTrackingStrategy.unregister(appContext)
        viewTreeTrackingStrategy.unregister(appContext)
    }

    // endregion
}
