package com.example.budgetapp.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetapp.databinding.ItemCategoryHeaderBinding

class HeaderAdapter(private val title: String) : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemCategoryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(title)
    }

    // Этот адаптер всегда содержит только один элемент - заголовок
    override fun getItemCount(): Int = 1

    inner class HeaderViewHolder(private val binding: ItemCategoryHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(headerTitle: String) {
            binding.textHeaderTitle.text = headerTitle
        }
    }
}