/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.domain.migration

/**
 * Описывает интерфейс классов миграции приложения.
 *
 * @property version версия приложения, начиная с которой должна выполняться миграция
 */
abstract class Migration(
    val version: Int
) {

    /**
     * Сообщение с описанием выполняемой миграции
     */
    val description: Int
        get() = getMigrationDescription()

    /**
     * Метод выполнения миграции.
     *
     * Сравнивает [текущую версию приложения, с которой обновляется пользователь][versionCode] и [версию миграции,
     * указанную для текущего объекта][version], и, если текущая версия приложения меньше,
     * запускает миграцию.
     */
    fun migrate(versionCode: Int) {
        if (versionCode < version) {
            doMigrate()
        }
    }

    /**
     * Получение описания выполняемого обновления
     *
     * @return идентификатор строки с описанием выполняемого обновления
     */
    protected abstract fun getMigrationDescription(): Int

    /**
     * Выполнение процедуры обновления
     */
    protected abstract fun doMigrate()
}
