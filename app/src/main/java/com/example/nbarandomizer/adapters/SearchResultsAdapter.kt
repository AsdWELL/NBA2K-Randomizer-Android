package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.callbacks.SearchPlayerResultsDiffCallback
import com.example.nbarandomizer.databinding.ItemSearchPlayerBinding
import com.example.nbarandomizer.models.SearchPlayerResult

class SearchResultViewHolder(private val binding: ItemSearchPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(searchPlayerResult: SearchPlayerResult) {
        with(binding) {
            name.text = searchPlayerResult.name
            team.text = searchPlayerResult.team

            Glide.with(binding.root)
                .load(searchPlayerResult.photoUrl)
                .signature(ObjectKey(searchPlayerResult.team))
                .into(photo)
        }
    }
}

class SearchResultsAdapter
    : ListAdapter<SearchPlayerResult, SearchResultViewHolder>(SearchPlayerResultsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchPlayerBinding.inflate(inflater, parent, false)

        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}