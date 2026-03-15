/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.data.config

import com.google.common.truth.Truth.assertThat
import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Тесты для [FeatureToggleRepositoryImpl]
 */
class FeatureToggleRepositoryImplTest {

    private val resources: Resources = mockk()
    private val context: Context = mockk(relaxed = true)

    private val repository = FeatureToggleRepositoryImpl(context, CONFIG_RES_ID)

    @Before
    fun setUp() {
        every { context.resources } returns resources
    }

    @Test
    fun getTogglesStream_withResourceReadSuccess() {
        every { resources.openRawResource(CONFIG_RES_ID) } returns ByteArrayInputStream(RESPONSE_STRING.toByteArray())

        assertThat(repository.getTogglesStream().readText()).isEqualTo(RESPONSE_STRING)
    }

    @Test
    fun getTogglesStream_withResourceReadFailure() {
        every { resources.openRawResource(CONFIG_RES_ID) } throws IOException()

        val thrown = runCatching { repository.getTogglesStream() }.exceptionOrNull()
        assertThat(thrown).isNotNull()
    }

    private fun InputStream.readText() = this.reader(Charsets.UTF_8).readText()

    private companion object {
        private const val CONFIG_RES_ID = 1
        private const val RESPONSE_STRING = "response"
    }
}
