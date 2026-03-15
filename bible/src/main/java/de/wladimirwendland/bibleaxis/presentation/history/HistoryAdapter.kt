/*
 * Copyright (C) 2011 Scripture Software and contributors
 * Copyright (C) 2026 Wladimir Wendland
 * SPDX-License-Identifier: Apache-2.0
 * Modified by BibleAxis contributors
 */

package de.wladimirwendland.bibleaxis.presentation.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.wladimirwendland.bibleaxis.R
import de.wladimirwendland.bibleaxis.entity.ItemList

class HistoryAdapter(
    private val items: List<ItemList>,
    private val clickListener: (ItemList) -> Unit,
    private val deleteClickListener: (ItemList) -> Unit,
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = items[position]
        holder.link.text = item[ItemList.Name]
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
        holder.deleteButton.setOnClickListener {
            deleteClickListener(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class HistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val link: TextView = itemView.findViewById(R.id.place_scripture)
        val deleteButton: ImageButton = itemView.findViewById(R.id.history_delete)
    }
}
