package com.recetalog.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.recetalog.R
import com.recetalog.databinding.ActivityRecetasBinding
import com.recetalog.databinding.DialogFilterBinding
import com.recetalog.model.Receta
import com.recetalog.model.RecetaAdapter
import com.recetalog.viewmodel.RecetaViewModel

class RecetasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecetasBinding
    private val viewModel: RecetaViewModel by viewModels()

    private lateinit var adapter: RecetaAdapter
    private var allRecetas: List<Receta> = emptyList()
    private var filtroActivo = FilterState()

    data class FilterState(
        var verdura: Boolean = false,
        var carne: Boolean = false,
        var pescado: Boolean = false,
        var postre: Boolean = false,
        var lactosa: Boolean = false,
        var fruta: Boolean = false
    ) {
        fun isActive() = verdura || carne || pescado || postre || lactosa || fruta
        fun reset() {
            verdura = false
            carne = false
            pescado = false
            postre = false
            lactosa = false
            fruta = false
        }
    }

    // Launcher para recibir resultado de AddRecetaActivity
    private val addRecetaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // La lista se actualiza automáticamente via LiveData
    }

    // Launcher para recibir resultado de DetalleRecetaActivity
    private val detalleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // La lista se actualiza automáticamente via LiveData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeRecetas()
    }

    private fun setupRecyclerView() {
        adapter = RecetaAdapter(
            recetas = emptyList(),
            onItemClick = { receta ->
                val intent = Intent(this, DetalleRecetaActivity::class.java).apply {
                    putExtra(DetalleRecetaActivity.EXTRA_RECETA_ID, receta.idreceta)
                }
                detalleLauncher.launch(intent)
            }
        )
        binding.rcvMenuRecetas.layoutManager = LinearLayoutManager(this)
        binding.rcvMenuRecetas.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnFiltro.setOnClickListener {
            showFilterDialog()
        }

        binding.btnAddRec.setOnClickListener {
            val intent = Intent(this, AddRecetaActivity::class.java)
            addRecetaLauncher.launch(intent)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun observeRecetas() {
        viewModel.getAllRecetas().observe(this) { recetas ->
            allRecetas = recetas
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        val recetasFiltradas = if (filtroActivo.isActive()) {
            allRecetas.filter { receta ->
                (filtroActivo.verdura && receta.isVerdura) ||
                (filtroActivo.carne && receta.isCarne) ||
                (filtroActivo.pescado && receta.isPescado) ||
                (filtroActivo.postre && receta.isPostre) ||
                (filtroActivo.lactosa && receta.isLactosa) ||
                (filtroActivo.fruta && receta.isFruta)
            }
        } else {
            allRecetas
        }
        adapter.updateRecetas(recetasFiltradas)
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogFilterBinding.inflate(LayoutInflater.from(this))

        dialogBinding.chipFiltroVerdura.isChecked = filtroActivo.verdura
        dialogBinding.chipFiltroCarne.isChecked = filtroActivo.carne
        dialogBinding.chipFiltroPescado.isChecked = filtroActivo.pescado
        dialogBinding.chipFiltroPostre.isChecked = filtroActivo.postre
        dialogBinding.chipFiltroLactosa.isChecked = filtroActivo.lactosa
        dialogBinding.chipFiltroFruta.isChecked = filtroActivo.fruta

        AlertDialog.Builder(this)
            .setTitle(R.string.filtrar_por)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.aplicar_filtro) { _, _ ->
                filtroActivo.verdura = dialogBinding.chipFiltroVerdura.isChecked
                filtroActivo.carne = dialogBinding.chipFiltroCarne.isChecked
                filtroActivo.pescado = dialogBinding.chipFiltroPescado.isChecked
                filtroActivo.postre = dialogBinding.chipFiltroPostre.isChecked
                filtroActivo.lactosa = dialogBinding.chipFiltroLactosa.isChecked
                filtroActivo.fruta = dialogBinding.chipFiltroFruta.isChecked
                aplicarFiltro()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .setNeutralButton(R.string.limpiar_filtro) { _, _ ->
                filtroActivo.reset()
                aplicarFiltro()
            }
            .show()
    }
}

