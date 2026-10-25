package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.databinding.ItemAttributeBinding
import com.example.nbarandomizer.models.Rating
import com.example.nbarandomizer.ui.components.RatingView

class SingleAttributeViewHolder(private val binding: ItemAttributeBinding) : RecyclerView.ViewHolder(binding.root) {
    private val ratingView = RatingView(binding.attributeCard, binding.attributeValue, binding.attributeName)

    fun bind(rating: Rating) {
        ratingView.bind(rating)
    }
}

class SingleAttributeAdapter : RecyclerView.Adapter<SingleAttributeViewHolder>() {
    var ratingsCollection: List<Rating> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleAttributeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAttributeBinding.inflate(inflater, parent, false)

        return SingleAttributeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return ratingsCollection.size
    }

    override fun onBindViewHolder(holder: SingleAttributeViewHolder, position: Int) {
        holder.bind(ratingsCollection[position])
    }
}
