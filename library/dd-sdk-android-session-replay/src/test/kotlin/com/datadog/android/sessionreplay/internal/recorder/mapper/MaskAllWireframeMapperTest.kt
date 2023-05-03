/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder.mapper

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.datadog.android.sessionreplay.forge.ForgeConfigurator
import com.datadog.android.sessionreplay.model.MobileSegment
import fr.xgouchet.elmyr.Forge
import fr.xgouchet.elmyr.junit5.ForgeConfiguration
import fr.xgouchet.elmyr.junit5.ForgeExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@Extensions(
    ExtendWith(MockitoExtension::class),
    ExtendWith(ForgeExtension::class)
)
@MockitoSettings(strictness = Strictness.LENIENT)
@ForgeConfiguration(ForgeConfigurator::class)
internal class MaskAllWireframeMapperTest : BaseGenericWireframeMapperTest() {

    @Mock
    lateinit var mockViewWireframeMapper: ViewWireframeMapper

    @Mock
    lateinit var mockImageWireframeMapper: ViewScreenshotWireframeMapper

    @Mock
    lateinit var mockMaskAllTextViewMapper: MaskAllTextViewMapper

    @Mock
    lateinit var mockButtonMapper: ButtonMapper

    @Mock
    lateinit var mockCheckedTextViewWireframeMapper: MaskAllCheckedTextViewMapper

    @Mock
    lateinit var mockEditTextViewMapper: EditTextViewMapper

    @Mock
    lateinit var mockDecorViewMapper: DecorViewMapper

    @Mock
    lateinit var mockCheckBoxWireframeMapper: MaskAllCheckBoxMapper

    @Mock
    lateinit var mockRadioButtonMapper: MaskAllRadioButtonMapper

    @Mock
    lateinit var mockSwitchCompatMapper: MaskAllSwitchCompatMapper

    lateinit var mockShapeWireframes: List<MobileSegment.Wireframe.ShapeWireframe>

    lateinit var mockImageWireframes: List<MobileSegment.Wireframe.ShapeWireframe>

    lateinit var mockMaskedTextWireframes: List<MobileSegment.Wireframe.TextWireframe>

    lateinit var mockButtonWireframes: List<MobileSegment.Wireframe.TextWireframe>

    lateinit var mockEditTextWireframes: List<MobileSegment.Wireframe>

    lateinit var mockCheckedTextWireframes: List<MobileSegment.Wireframe>

    lateinit var mockDecorViewWireframes: List<MobileSegment.Wireframe.ShapeWireframe>

    lateinit var mockCheckBoxWireframes: List<MobileSegment.Wireframe>

    lateinit var mockRadioButtonWireframes: List<MobileSegment.Wireframe>

    lateinit var mockSwitchCompatWireframes: List<MobileSegment.Wireframe>

    lateinit var testedMaskAllWireframeMapper: MaskAllWireframeMapper

    @BeforeEach
    override fun `set up`(forge: Forge) {
        super.`set up`(forge)
        mockShapeWireframes = forge.aList { mock() }
        mockImageWireframes = forge.aList { mock() }
        mockButtonWireframes = forge.aList { mock() }
        mockShapeWireframes = forge.aList { mock() }
        mockMaskedTextWireframes = forge.aList { mock() }
        mockEditTextWireframes = forge.aList { mock() }
        mockCheckedTextWireframes = forge.aList { mock() }
        mockDecorViewWireframes = forge.aList { mock() }
        mockCheckBoxWireframes = forge.aList { mock() }
        mockRadioButtonWireframes = forge.aList { mock() }
        mockSwitchCompatWireframes = forge.aList { mock() }
        testedMaskAllWireframeMapper = MaskAllWireframeMapper(
            mockViewWireframeMapper,
            mockImageWireframeMapper,
            mockMaskAllTextViewMapper,
            mockButtonMapper,
            mockEditTextViewMapper,
            mockCheckedTextViewWireframeMapper,
            mockDecorViewMapper,
            mockCheckBoxWireframeMapper,
            mockRadioButtonMapper,
            mockSwitchCompatMapper,
            mockCustomMappers
        )
    }

    @Test
    fun `M resolve first from customMappers W map { customMappers provided }`(forge: Forge) {
        // Given
        mockCustomMappers = mockCustomMappedToData.associate {
            val mockWireframeMapper = mock<WireframeMapper<View, *>>()
            whenever(mockWireframeMapper.map(it.first, fakeSystemInformation)).thenReturn(it.second)
            it.first::class.java to mockWireframeMapper
        }
        val fakeCustomMappedIndex = forge.anInt(min = 0, max = mockCustomMappedToData.size)
        val mockCustomMappedView = mockCustomMappedToData[fakeCustomMappedIndex].first
        val expectedWireframeData = mockCustomMappedToData[fakeCustomMappedIndex].second
        testedMaskAllWireframeMapper = MaskAllWireframeMapper(
            mockViewWireframeMapper,
            mockImageWireframeMapper,
            mockMaskAllTextViewMapper,
            mockButtonMapper,
            mockEditTextViewMapper,
            mockCheckedTextViewWireframeMapper,
            mockDecorViewMapper,
            mockCheckBoxWireframeMapper,
            mockRadioButtonMapper,
            mockSwitchCompatMapper,
            mockCustomMappers
        )

        // Then
        assertThat(testedMaskAllWireframeMapper.map(mockCustomMappedView, fakeSystemInformation))
            .isEqualTo(expectedWireframeData)
    }

    @Test
    fun `M resolve a ShapeWireframe W map { non DecorView }`() {
        // Given
        val mockView: View = mockNonDecorView()
        whenever(mockViewWireframeMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockShapeWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockShapeWireframes)
    }

    @Test
    fun `M resolve a ShapeWireframe W map { ImageView }`() {
        // Given
        val mockView: ImageView = mock()
        whenever(mockImageWireframeMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockImageWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockImageWireframes)
    }

    @Test
    fun `M resolve a masked TextWireframe W map { TextView }`() {
        // Given
        val mockView: TextView = mock()
        whenever(mockMaskAllTextViewMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockMaskedTextWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockMaskedTextWireframes)
    }

    @Test
    fun `M resolve a ButtonWireframe W map { Button }`() {
        // Given
        val mockView: Button = mock()
        whenever(mockButtonMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockButtonWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockButtonWireframes)
    }

    @Test
    fun `M delegate to EditTextWireframeMapper W map { EditText }`() {
        // Given
        val mockView: EditText = mock()
        whenever(mockEditTextViewMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockEditTextWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockEditTextWireframes)
    }

    @Test
    fun `M delegate to CheckedTextWireframeMapper W map { CheckedTextView }`() {
        // Given
        val mockView: CheckedTextView = mock()
        whenever(mockCheckedTextViewWireframeMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockCheckedTextWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockCheckedTextWireframes)
    }

    @Test
    fun `M delegate to CheckBoxWireframeMapper W map { CheckBox }`() {
        // Given
        val mockView: CheckBox = mock()
        whenever(mockCheckBoxWireframeMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockCheckBoxWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockCheckBoxWireframes)
    }

    @Test
    fun `M delegate to RadioButtonWireframeMapper W map { RadioButton }`() {
        // Given
        val mockView: RadioButton = mock()
        whenever(mockRadioButtonMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockRadioButtonWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockRadioButtonWireframes)
    }

    @Test
    fun `M delegate to SwitchCompatWireframeMapper W map { SwitchCompat }`() {
        // Given
        val mockView: SwitchCompat = mock()
        whenever(mockSwitchCompatMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockSwitchCompatWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockSwitchCompatWireframes)
    }

    @Test
    fun `M return the ImageMapper W getImageMapper`() {
        assertThat(testedMaskAllWireframeMapper.imageMapper).isEqualTo(mockImageWireframeMapper)
    }

    @Test
    fun `M delegate to DecorViewWireframeMapper W map { view with no parent }`() {
        // Given
        val mockView: View = mockViewWithEmptyParent()
        whenever(mockDecorViewMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockDecorViewWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockDecorViewWireframes)
    }

    @Test
    fun `M delegate to DecorViewWireframeMapper W map { view with parent has no View type }`() {
        // Given
        val mockView: View = mockViewWithNoViewTypeParent()
        whenever(mockDecorViewMapper.map(mockView, fakeSystemInformation))
            .thenReturn(mockDecorViewWireframes)

        // When
        val wireframes = testedMaskAllWireframeMapper.map(mockView, fakeSystemInformation)

        // Then
        assertThat(wireframes).isEqualTo(mockDecorViewWireframes)
    }
}
