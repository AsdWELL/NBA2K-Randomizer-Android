package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.databinding.ItemPlayerAttributesBinding
import com.example.nbarandomizer.models.AttributeRatings
import com.example.nbarandomizer.ui.components.RatingView

class AttributesViewHolder(private val binding: ItemPlayerAttributesBinding) : RecyclerView.ViewHolder(binding.root) {
    private val ratingView = RatingView(binding.attributeCard, binding.attributeValue, binding.attributeName)

    fun bind(attributeRatings: AttributeRatings) {
        val singleAttributeAdapter = SingleAttributeAdapter()
        singleAttributeAdapter.ratingsCollection = attributeRatings.parameters

        with(binding) {
            ratingView.bind(attributeRatings.rating)

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
