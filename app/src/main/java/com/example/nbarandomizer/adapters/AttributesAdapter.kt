package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.databinding.ItemPlayerAttributesBinding
import com.example.nbarandomizer.models.AttributeRatings

class AttributesViewHolder(private val binding: ItemPlayerAttributesBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(attributeRatings: AttributeRatings) {
        val singleAttributeAdapter = SingleAttributeAdapter()
        singleAttributeAdapter.ratingsCollection = attributeRatings.parameters

        with(binding) {
            attributeCard.bind(attributeRatings.rating, attributeName)

            singleAttributeRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            singleAttributeRecyclerView.adapter = singleAttributeAdapter
        }
    }
}

class AttributesAdapter : RecyclerView.Adapter<AttributesViewHolder>() {
    var attributesCollections: List<AttributeRatings> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlayerAttributesBinding.inflate(inflater, parent, false)

        return AttributesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return attributesCollections.size
    }

    override fun onBindViewHolder(holder: AttributesViewHolder, position: Int) {
        holder.bind(attributesCollections[position])
    }
}
