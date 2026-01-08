package com.recetalog.view

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.recetalog.R
import com.recetalog.databinding.ActivityDetalleRecetaBinding
import com.recetalog.model.Receta
import com.recetalog.viewmodel.RecetaViewModel
import kotlinx.coroutines.launch

class DetalleRecetaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalleRecetaBinding
    private val viewModel: RecetaViewModel by viewModels()

    private var currentReceta: Receta? = null

    companion object {
        const val EXTRA_RECETA_ID = "extra_receta_id"
    }

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getIntExtra(EXTRA_RECETA_ID, -1)
        if (recetaId == -1) {
            finish()
            return
        }

        loadRecetaData(recetaId)
        setupListeners()
    }

    private fun loadRecetaData(id: Int) {
        viewModel.getRecetaById(id).observe(this) { receta ->
            receta?.let {
                currentReceta = it
                displayReceta(it)
            }
        }
    }

    private fun displayReceta(receta: Receta) {
        binding.txtNombreRecetaDetalle.text = receta.nmreceta
        binding.txtIngredientesDetalle.text = receta.ingredientes
        binding.txtPasosDetalle.text = receta.pasos

        if (receta.imagen != null && receta.imagen.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
            binding.imgRecetaDetalle.setImageBitmap(bitmap)
        } else {
            binding.imgRecetaDetalle.setImageResource(R.mipmap.logoapp)
        }

        binding.chipVerduraDetalle.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
        binding.chipCarneDetalle.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
        binding.chipPescadoDetalle.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
        binding.chipPostreDetalle.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
        binding.chipLactosaDetalle.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
        binding.chipFrutaDetalle.visibility = if (receta.isFruta) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.btnEditarReceta.setOnClickListener {
            currentReceta?.let { receta ->
                val intent = Intent(this, AddRecetaActivity::class.java).apply {
                    putExtra(AddRecetaActivity.EXTRA_RECETA_ID, receta.idreceta)
                }
                editLauncher.launch(intent)
            }
        }

        binding.btnEliminarReceta.setOnClickListener {
            currentReceta?.let { receta ->
                lifecycleScope.launch {
                    viewModel.delete(receta)
                    Toast.makeText(
                        this@DetalleRecetaActivity,
                        getString(R.string.msg_receta_eliminada),
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

