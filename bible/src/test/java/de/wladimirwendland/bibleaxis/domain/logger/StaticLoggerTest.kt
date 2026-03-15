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

class StaticLoggerTest {

    private val logger: Logger = mockk(relaxed = true)
    private val objectTag = "${this.javaClass.simpleName}(${this.hashCode()})"

    @Before
    fun setUp() {
        StaticLogger.init(logger)
    }

    @Test
    fun debug() {
        StaticLogger.debug(TAG, MESSAGE)
        verify { logger.debug(TAG, MESSAGE) }
    }

    @Test
    fun debugWithObjectTag() {
        StaticLogger.debug(this, MESSAGE)
        verify { logger.debug(objectTag, MESSAGE) }
    }

    @Test
    fun error() {
        StaticLogger.error(TAG, MESSAGE)
        verify { logger.error(TAG, MESSAGE) }
    }

    @Test
    fun errorWithObjectTag() {
        StaticLogger.error(this, MESSAGE)
        verify { logger.error(objectTag, MESSAGE) }
    }

    @Test
    fun errorWithThrowable() {
        val th: Throwable = IndexOutOfBoundsException()
        StaticLogger.error(TAG, MESSAGE, th)
        verify { logger.error(TAG, MESSAGE, th) }
    }

    @Test
    fun errorWithThrowableAndObjectTag() {
        val th: Throwable = IndexOutOfBoundsException()
        StaticLogger.error(this, MESSAGE, th)
        verify { logger.error(objectTag, MESSAGE, th) }
    }

    @Test
    fun info() {
        StaticLogger.info(TAG, MESSAGE)
        verify { logger.info(TAG, MESSAGE) }
    }

    @Test
    fun infoWithObjectTag() {
        StaticLogger.info(this, MESSAGE)
        verify { logger.info(objectTag, MESSAGE) }
    }

    @Test
    fun warn() {
        StaticLogger.warn(TAG, MESSAGE)
        verify { logger.warn(TAG, MESSAGE) }
    }

    @Test
    fun warnWithObjectTag() {
        StaticLogger.warn(this, MESSAGE)
        verify { logger.warn(objectTag, MESSAGE) }
    }

    @Test
    fun nonInit() {
        StaticLogger.init(null)
        StaticLogger.info(TAG, MESSAGE)
    }

    @Test
    fun nonInitWithObjectTag() {
        StaticLogger.init(null)
        StaticLogger.info(this, MESSAGE)
    }

    companion object {
        private const val TAG = "StaticLoggerTest"
        private const val MESSAGE = "test message"
    }
}