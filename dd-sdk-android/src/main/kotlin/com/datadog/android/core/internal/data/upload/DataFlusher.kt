/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.core.internal.data.upload

import androidx.annotation.WorkerThread
import com.datadog.android.core.internal.net.DataUploader
import com.datadog.android.core.internal.persistence.PayloadDecoration
import com.datadog.android.core.internal.persistence.file.ChunkedFileHandler
import com.datadog.android.core.internal.persistence.file.FileOrchestrator
import com.datadog.android.core.internal.utils.join

internal class DataFlusher(
    internal val fileOrchestrator: FileOrchestrator,
    internal val decoration: PayloadDecoration,
    internal val handler: ChunkedFileHandler
) : Flusher {

    @WorkerThread
    override fun flush(uploader: DataUploader) {
        val toUploadFiles = fileOrchestrator.getFlushableFiles()
        toUploadFiles.forEach {
            val batch = handler.readData(it)
                .join(
                    separator = decoration.separatorBytes,
                    prefix = decoration.prefixBytes,
                    suffix = decoration.suffixBytes
                )
            uploader.upload(batch)
            handler.delete(it)
        }
    }
}
