/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.wladimirwendland.bibleaxis.di.scope.PerActivity
import de.wladimirwendland.bibleaxis.entity.ItemList
import de.wladimirwendland.bibleaxis.managers.Librarian
import javax.inject.Inject

class HistoryViewModel(
    private val myLibrarian: Librarian,
) : ViewModel() {

    private val _historyState = MutableLiveData<HistoryViewResult>()
    val historyState: LiveData<HistoryViewResult>
        get() = _historyState

    fun onActivityCreate() {
        updateHistoryList()
    }

    fun onClickList(item: ItemList) {
        item[ItemList.ID]?.let { link ->
            _historyState.value = HistoryViewResult.OpenLink(link)
        }
    }

    fun onClickClearHistory() {
        myLibrarian.clearHistory()
        updateHistoryList()
    }

    fun onDeleteHistoryItem(item: ItemList) {
        myLibrarian.deleteHistoryItem(item)
        updateHistoryList()
    }

    private fun updateHistoryList() {
        val historyList = myLibrarian.historyList
        _historyState.value = HistoryViewResult.HistoryList(historyList)
    }

    @PerActivity
    class Factory @Inject constructor(
        private val myLibrarian: Librarian,
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass : Class<T>): T =
            HistoryViewModel(myLibrarian) as T
    }
}
