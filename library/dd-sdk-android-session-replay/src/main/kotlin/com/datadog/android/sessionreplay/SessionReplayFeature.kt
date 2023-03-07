/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay

import android.app.Application
import android.content.Context
import com.datadog.android.core.internal.utils.internalLogger
import com.datadog.android.sessionreplay.internal.LifecycleCallback
import com.datadog.android.sessionreplay.internal.NoOpLifecycleCallback
import com.datadog.android.sessionreplay.internal.RecordWriter
import com.datadog.android.sessionreplay.internal.SessionReplayLifecycleCallback
import com.datadog.android.sessionreplay.internal.SessionReplayRecordCallback
import com.datadog.android.sessionreplay.internal.SessionReplayRumContextProvider
import com.datadog.android.sessionreplay.internal.domain.SessionReplayRequestFactory
import com.datadog.android.sessionreplay.internal.storage.NoOpRecordWriter
import com.datadog.android.sessionreplay.internal.storage.SessionReplayRecordWriter
import com.datadog.android.sessionreplay.internal.time.SessionReplayTimeProvider
import com.datadog.android.v2.api.Feature
import com.datadog.android.v2.api.FeatureEventReceiver
import com.datadog.android.v2.api.FeatureStorageConfiguration
import com.datadog.android.v2.api.InternalLogger
import com.datadog.android.v2.api.RequestFactory
import com.datadog.android.v2.api.SdkCore
import com.datadog.android.v2.api.StorageBackedFeature
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Session Replay feature class, which needs to be registered with Datadog SDK instance.
 */
class SessionReplayFeature internal constructor(
    configuration: SessionReplayConfiguration,
    private val sessionReplayCallbackProvider: (SdkCore, RecordWriter) -> LifecycleCallback
) : StorageBackedFeature, FeatureEventReceiver {

    /**
     * Creates Session Replay feature.
     *
     * @param configuration Session Replay configuration, which can be created
     * using [SessionReplayConfiguration.Builder].
     */
    constructor(configuration: SessionReplayConfiguration) : this(
        configuration,
        { sdkCore, recordWriter ->
            SessionReplayLifecycleCallback(
                rumContextProvider = SessionReplayRumContextProvider(sdkCore),
                privacy = configuration.privacy,
                recordWriter = recordWriter,
                timeProvider = SessionReplayTimeProvider(sdkCore),
                recordCallback = SessionReplayRecordCallback(sdkCore),
                customMappers = configuration.customMappers()
            )
        }
    )

    internal lateinit var appContext: Context
    internal lateinit var sdkCore: SdkCore
    private var isRecording = AtomicBoolean(false)
    internal var sessionReplayCallback: LifecycleCallback = NoOpLifecycleCallback()

    internal var dataWriter: RecordWriter = NoOpRecordWriter()
    internal val initialized = AtomicBoolean(false)

    // region Feature

    override val name: String = Feature.SESSION_REPLAY_FEATURE_NAME

    override fun onInitialize(
        sdkCore: SdkCore,
        appContext: Context
    ) {
        this.sdkCore = sdkCore
        this.appContext = appContext

        sdkCore.setEventReceiver(SESSION_REPLAY_FEATURE_NAME, this)
        dataWriter = createDataWriter()
        sessionReplayCallback = sessionReplayCallbackProvider(sdkCore, dataWriter)
        initialized.set(true)
        startRecording()
    }

    override val requestFactory: RequestFactory =
        SessionReplayRequestFactory(configuration.endpointUrl)

    override val storageConfiguration: FeatureStorageConfiguration =
        FeatureStorageConfiguration.DEFAULT

    override fun onStop() {
        stopRecording()
        dataWriter = NoOpRecordWriter()
        sessionReplayCallback = NoOpLifecycleCallback()
        initialized.set(false)
    }

    // endregion

    private fun createDataWriter(): RecordWriter {
        return SessionReplayRecordWriter(sdkCore)
    }

    // region SessionReplayFeature

    /**
     * Stops the session recording.
     *
     * Session Replay feature will only work for recorded
     * sessions.
     */
    fun stopRecording() {
        if (isRecording.getAndSet(false)) {
            unregisterCallback(appContext)
        }
    }

    /**
     * Starts/resumes the session recording.
     *
     * Session Replay feature will only work for recorded
     * sessions.
     */
    fun startRecording() {
        if (!initialized.get()) {
            internalLogger.log(
                InternalLogger.Level.WARN,
                InternalLogger.Target.USER,
                CANNOT_START_RECORDING_NOT_INITIALIZED
            )
            return
        }
        if (!isRecording.getAndSet(true)) {
            registerCallback(appContext)
        }
    }

    // endregion

    // region EventReceiver

    override fun onReceive(event: Any) {
        if (event !is Map<*, *>) {
            internalLogger.log(
                InternalLogger.Level.WARN,
                InternalLogger.Target.USER,
                UNSUPPORTED_EVENT_TYPE.format(Locale.US, event::class.java.canonicalName)
            )
            return
        }

        if (event[SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY] == RUM_SESSION_RENEWED_BUS_MESSAGE) {
            val keepSession = event[RUM_KEEP_SESSION_BUS_MESSAGE_KEY] as? Boolean

            if (keepSession == null) {
                internalLogger.log(
                    InternalLogger.Level.WARN,
                    InternalLogger.Target.USER,
                    EVENT_MISSING_MANDATORY_FIELDS
                )
                return
            }

            if (keepSession) {
                startRecording()
            } else {
                stopRecording()
            }
        } else {
            internalLogger.log(
                InternalLogger.Level.WARN,
                InternalLogger.Target.USER,
                UNKNOWN_EVENT_TYPE_PROPERTY_VALUE.format(
                    Locale.US,
                    event[SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY]
                )
            )
        }
    }

    // endregion

    // region Internal

    private fun registerCallback(context: Context) {
        if (context is Application) {
            sessionReplayCallback.register(context)
        }
    }

    private fun unregisterCallback(context: Context) {
        if (context is Application) {
            sessionReplayCallback.unregisterAndStopRecorders(context)
        }
    }

    // endregion

    internal companion object {
        internal const val UNSUPPORTED_EVENT_TYPE =
            "Session Replay feature receive an event of unsupported type=%s."
        internal const val UNKNOWN_EVENT_TYPE_PROPERTY_VALUE =
            "Session Replay feature received an event with unknown value of \"type\" property=%s."
        internal const val EVENT_MISSING_MANDATORY_FIELDS = "Session Replay feature received an " +
            "event where one or more mandatory (keepSession) fields" +
            " are either missing or have wrong type."
        internal const val CANNOT_START_RECORDING_NOT_INITIALIZED =
            "Cannot start session recording, because Session Replay feature is not initialized."
        const val SESSION_REPLAY_FEATURE_NAME = "session-replay"
        const val SESSION_REPLAY_BUS_MESSAGE_TYPE_KEY = "type"
        const val RUM_SESSION_RENEWED_BUS_MESSAGE = "rum_session_renewed"
        const val RUM_KEEP_SESSION_BUS_MESSAGE_KEY = "keepSession"
    }
}
