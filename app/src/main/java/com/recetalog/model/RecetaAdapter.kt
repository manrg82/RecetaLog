package com.recetalog.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.recetalog.databinding.RecetaItemBinding
import com.recetalog.view.RecetaViewHolder

class RecetaAdapter(
    private var recetas: List<Receta>,
    private val onItemClick: (Receta) -> Unit = {}
) : RecyclerView.Adapter<RecetaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = RecetaItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position], onItemClick)
    }

    override fun getItemCount(): Int = recetas.size

    fun updateRecetas(newRecetas: List<Receta>) {
        recetas = newRecetas
        notifyDataSetChanged()
    }
}