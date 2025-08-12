package com.example.nbarandomizer.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.nbarandomizer.models.SearchPlayerResult

class SearchPlayerResultsDiffCallback : DiffUtil.ItemCallback<SearchPlayerResult>() {
    override fun areItemsTheSame(
        oldItem: SearchPlayerResult,
        newItem: SearchPlayerResult
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: SearchPlayerResult,
        newItem: SearchPlayerResult
    ): Boolean {
        return oldItem == newItem
    }
}