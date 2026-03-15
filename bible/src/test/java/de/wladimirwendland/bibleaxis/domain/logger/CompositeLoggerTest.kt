/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.logger

import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Тесты для [CompositeLogger]
 */
class CompositeLoggerTest {

    private val logger1: Logger = mockk(relaxed = true)
    private val logger2: Logger = mockk(relaxed = true)

    private lateinit var logger: CompositeLogger

    @Before
    fun setUp() {
        logger = CompositeLogger(listOf(logger1, logger2))
    }

    @Test
    fun debug() {
        logger.debug(TAG, MESSAGE)
        verify {
            logger1.debug(TAG, MESSAGE)
            logger2.debug(TAG, MESSAGE)
        }
    }

    @Test
    fun error() {
        val th: Throwable = IndexOutOfBoundsException()
        logger.error(TAG, MESSAGE, th)
        verify {
            logger1.error(TAG, MESSAGE, th)
            logger2.error(TAG, MESSAGE, th)
        }
    }

    @Test
    fun errorWithThrowable() {
        logger.error(TAG, MESSAGE)
        verify {
            logger1.error(TAG, MESSAGE)
            logger2.error(TAG, MESSAGE)
        }
    }

    @Test
    fun info() {
        logger.info(TAG, MESSAGE)
        verify {
            logger1.info(TAG, MESSAGE)
            logger2.info(TAG, MESSAGE)
        }
    }

    companion object {
        private const val TAG = "CompositeLoggerTest"
        private const val MESSAGE = "test message"
    }
}