/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.migration

import de.wladimirwendland.bibleaxis.BuildConfig
import de.wladimirwendland.bibleaxis.utils.PreferenceHelper
import io.reactivex.Observable
import de.wladimirwendland.bibleaxis.domain.logger.StaticLogger

/**
 * Класс отвечающий за выполнение [Migration] при обновлении версии приложения
 */
class UpdateManager(
    private val prefHelper: PreferenceHelper,
    private val migrationList: Set<Migration>
) {

    fun update(): Observable<Int> {
        return Observable.create { emitter ->
            StaticLogger.info(this, "Start update manager...")
            val currVersionCode = prefHelper.getInt("versionCode")
            if (BuildConfig.VERSION_CODE > currVersionCode) {
                migrationList
                    .filter { it.version > currVersionCode }
                    .sortedBy { it.version }
                    .forEach {
                        emitter.onNext(it.description)
                        it.migrate(currVersionCode)
                    }
                prefHelper.saveInt("versionCode", BuildConfig.VERSION_CODE)
                StaticLogger.info(this, "Update success")
            }
            emitter.onComplete()
        }
    }
}
