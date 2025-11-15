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

/**
 * Adapter para mostrar recetas en un RecyclerView.
 * Vincula datos de recetas a las vistas del RecyclerView.
 */
class RecetaAdapter(
    private var recetas: List<Receta>,
    private val onItemClick: (Receta) -> Unit = {}
) : RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

    // Crea un nuevo ViewHolder para mostrar una receta
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.receta_item, parent, false)
        return RecetaViewHolder(view)
    }

    // Vincula los datos de una receta con su ViewHolder
    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position], onItemClick)
    }

    // Retorna la cantidad total de recetas
    override fun getItemCount(): Int = recetas.size

    // Actualiza la lista de recetas y notifica cambios
    fun updateRecetas(newRecetas: List<Receta>) {
        recetas = newRecetas
        notifyDataSetChanged()
    }

    /**
     * ViewHolder que mantiene referencias a las vistas de una receta.
     */
    class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgReceta: ImageView = itemView.findViewById(R.id.imgReceta)
        private val txtNombreReceta: TextView = itemView.findViewById(R.id.txtNombreReceta)
        private val chipVerdura: Chip = itemView.findViewById(R.id.chipVerdura)
        private val chipCarne: Chip = itemView.findViewById(R.id.chipCarne)
        private val chipPescado: Chip = itemView.findViewById(R.id.chipPescado)
        private val chipPostre: Chip = itemView.findViewById(R.id.chipPostre)
        private val chipLactosa: Chip = itemView.findViewById(R.id.chipLactosa)
        private val chipFruta: Chip = itemView.findViewById(R.id.chipFruta)

        // Rellena las vistas con datos de la receta
        fun bind(receta: Receta, onItemClick: (Receta) -> Unit) {
            // Establece el nombre de la receta
            txtNombreReceta.text = receta.nmreceta

            // Carga la imagen: si existe, la decodifica del ByteArray; si no, usa el logo
            if (receta.imagen != null && receta.imagen.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
                imgReceta.setImageBitmap(bitmap)
            } else {
                imgReceta.setImageResource(R.mipmap.logoapp)
            }

            // Muestra u oculta los chips de etiquetas según las propiedades de la receta
            chipVerdura.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
            chipCarne.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
            chipPescado.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
            chipPostre.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
            chipLactosa.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
            chipFruta.visibility = if (receta.isFruta) View.VISIBLE else View.GONE

            // Añade listener para cuando se hace clic en la receta
            itemView.setOnClickListener { onItemClick(receta) }
        }
    }
}