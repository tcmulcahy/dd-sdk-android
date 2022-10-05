/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.domain

import com.datadog.android.core.internal.persistence.PayloadDecoration
import com.datadog.android.core.internal.persistence.file.FileMover
import com.datadog.android.core.internal.persistence.file.FilePersistenceConfig
import com.datadog.android.core.internal.persistence.file.FileReaderWriter
import com.datadog.android.core.internal.persistence.file.advanced.FeatureFileOrchestrator
import com.datadog.android.core.internal.persistence.file.batch.BatchFilePersistenceStrategy
import com.datadog.android.core.internal.persistence.file.batch.BatchFileReaderWriter
import com.datadog.android.core.internal.privacy.ConsentProvider
import com.datadog.android.log.Logger
import com.datadog.android.security.Encryption
import com.datadog.android.sessionreplay.internal.SessionReplayFeature
import com.datadog.android.v2.core.internal.ContextProvider
import java.io.File
import java.util.concurrent.ExecutorService

internal class SessionReplayRecordPersistenceStrategy(
    contextProvider: ContextProvider,
    consentProvider: ConsentProvider,
    storageDir: File,
    executorService: ExecutorService,
    internalLogger: Logger,
    localDataEncryption: Encryption?,
    filePersistenceConfig: FilePersistenceConfig
) : BatchFilePersistenceStrategy<String>(
    contextProvider,
    FeatureFileOrchestrator(
        consentProvider,
        storageDir,
        SessionReplayFeature.SESSION_REPLAY_FEATURE_NAME,
        executorService,
        internalLogger
    ),
    executorService,
    SessionReplayRecordSerializer(),
    PayloadDecoration.NEW_LINE_DECORATION,
    internalLogger,
    BatchFileReaderWriter.create(internalLogger, localDataEncryption),
    FileReaderWriter.create(internalLogger, localDataEncryption),
    FileMover(internalLogger),
    filePersistenceConfig
)
