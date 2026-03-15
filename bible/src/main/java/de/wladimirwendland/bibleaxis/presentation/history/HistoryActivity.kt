/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.history

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.di.component.ActivityComponent
import de.wladimirwendland.bibleaxis.entity.ItemList
import de.wladimirwendland.bibleaxis.presentation.dialogs.DialogUiHelper
import de.wladimirwendland.bibleaxis.presentation.ui.base.BaseAppActivity
import javax.inject.Inject

class HistoryActivity : BaseAppActivity() {

    private val recyclerViewHistoryList: RecyclerView by lazy {
        findViewById(R.id.historyRecyclerView)
    }

    private val viewModel: HistoryViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: HistoryViewModel.Factory

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        recyclerViewHistoryList.addItemDecoration(itemDecoration)

        viewModel.historyState.observe(this) {
            when (it) {
                is HistoryViewResult.HistoryList -> setRecyclerViewAdapter(it.list)
                is HistoryViewResult.OpenLink -> sendResult(it.link)
            }
        }
        viewModel.onActivityCreate()
    }

    private fun sendResult(link: String) {
        val intent = Intent()
        intent.putExtra("linkOSIS", link)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun inject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val infl = menuInflater
        infl.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_bar_history_clear -> {
                viewModel.onClickClearHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setRecyclerViewAdapter(list: List<ItemList>) {
        val adapter = HistoryAdapter(
            list,
            clickListener = { viewModel.onClickList(it) },
            deleteClickListener = { item ->
                AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.history)
                    .setView(DialogUiHelper.createMessageView(this, getString(R.string.history_delete_question)))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.onDeleteHistoryItem(item)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        )
        recyclerViewHistoryList.adapter = adapter
    }
}
