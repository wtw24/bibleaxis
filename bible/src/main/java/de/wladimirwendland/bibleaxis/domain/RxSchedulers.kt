/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

data class RxSchedulers(
    val io: Scheduler = Schedulers.io(),
    val computation: Scheduler = Schedulers.computation(),
    val mainThread: Scheduler = AndroidSchedulers.mainThread()
)
