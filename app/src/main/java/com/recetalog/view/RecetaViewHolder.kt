package com.recetalog.view

import android.graphics.BitmapFactory
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.recetalog.R
import com.recetalog.databinding.RecetaItemBinding
import com.recetalog.model.Receta

class RecetaViewHolder(private val binding: RecetaItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(receta: Receta, onItemClick: (Receta) -> Unit) {
        binding.txtNombreReceta.text = receta.nmreceta

        if (receta.imagen != null && receta.imagen.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
            binding.imgReceta.setImageBitmap(bitmap)
        } else {
            binding.imgReceta.setImageResource(R.mipmap.logoapp)
        }

        binding.chipVerdura.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
        binding.chipCarne.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
        binding.chipPescado.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
        binding.chipPostre.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
        binding.chipLactosa.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
        binding.chipFruta.visibility = if (receta.isFruta) View.VISIBLE else View.GONE

        binding.root.setOnClickListener { onItemClick(receta) }
    }
}

