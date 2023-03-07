/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder.mapper

import android.view.View

internal class MaskAllWireframeMapper(
    viewWireframeMapper: ViewWireframeMapper = ViewWireframeMapper(),
    imageMapper: ViewScreenshotWireframeMapper = ViewScreenshotWireframeMapper(viewWireframeMapper),
    textMapper: MaskAllTextViewMapper = MaskAllTextViewMapper(),
    buttonMapper: ButtonMapper = ButtonMapper(textMapper),
    editTextViewMapper: EditTextViewMapper = EditTextViewMapper(textMapper),
    checkedTextViewWireframeMapper: MaskAllCheckedTextViewMapper =
        MaskAllCheckedTextViewMapper(textMapper),
    decorViewMapper: DecorViewMapper =
        DecorViewMapper(viewWireframeMapper),
    checkBoxWireframeMapper: MaskAllCheckBoxMapper =
        MaskAllCheckBoxMapper(textMapper),
    radioButtonMapper: MaskAllRadioButtonMapper = MaskAllRadioButtonMapper(textMapper),
    switchCompatMapper: SwitchCompatMapper = SwitchCompatMapper(textMapper),
    customMappers: Map<Class<*>, WireframeMapper<View, *>> = emptyMap()
) : GenericWireframeMapper(
    viewWireframeMapper,
    imageMapper,
    textMapper,
    buttonMapper,
    editTextViewMapper,
    checkedTextViewWireframeMapper,
    decorViewMapper,
    checkBoxWireframeMapper,
    radioButtonMapper,
    switchCompatMapper,
    customMappers
)
