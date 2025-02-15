/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.logs.integration

import com.datadog.android.api.context.UserInfo
import com.datadog.android.log.Logger
import com.datadog.android.logs.integration.tests.elmyr.LogsIntegrationForgeConfigurator
import com.datadog.tools.unit.extensions.TestConfigurationExtension
import fr.xgouchet.elmyr.annotation.Forgery
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.junit.jupiter.MockitoExtension

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class),
    ExtendWith(TestConfigurationExtension::class)
)
@ForgeConfiguration(LogsIntegrationForgeConfigurator::class)
class ScaffoldingTest {

    @Test
    fun `scaffolding test`(
        @Forgery fakeUserInfo: UserInfo
    ) {
        val logger = Logger.Builder().build()
        logger.i("Hello ${fakeUserInfo.name}…")
    }
}
