package com.recetalog.model

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.recetalog.R

class RecetaAdapter(
    private var recetas: List<Receta>,
    private val onItemClick: (Receta) -> Unit = {}
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.receta_item, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position], onItemClick)
    }

    override fun getItemCount(): Int = recetas.size

    fun updateRecetas(newRecetas: List<Receta>) {
        recetas = newRecetas
        notifyDataSetChanged()
    }

    class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgReceta: ImageView = itemView.findViewById(R.id.imgReceta)
        private val txtNombreReceta: TextView = itemView.findViewById(R.id.txtNombreReceta)
        private val chipVerdura: Chip = itemView.findViewById(R.id.chipVerdura)
        private val chipCarne: Chip = itemView.findViewById(R.id.chipCarne)
        private val chipPescado: Chip = itemView.findViewById(R.id.chipPescado)
        private val chipPostre: Chip = itemView.findViewById(R.id.chipPostre)
        private val chipLactosa: Chip = itemView.findViewById(R.id.chipLactosa)
        private val chipFruta: Chip = itemView.findViewById(R.id.chipFruta)

        fun bind(receta: Receta, onItemClick: (Receta) -> Unit) {
            // Nombre
            txtNombreReceta.text = receta.nmreceta

            // Imagen
            if (receta.imagen != null && receta.imagen.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
                imgReceta.setImageBitmap(bitmap)
            } else {
                imgReceta.setImageResource(R.mipmap.logoapp)
            }

            // Etiquetas
            chipVerdura.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
            chipCarne.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
            chipPescado.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
            chipPostre.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
            chipLactosa.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
            chipFruta.visibility = if (receta.isFruta) View.VISIBLE else View.GONE


            // Click listener
            itemView.setOnClickListener { onItemClick(receta) }
        }
    }
}