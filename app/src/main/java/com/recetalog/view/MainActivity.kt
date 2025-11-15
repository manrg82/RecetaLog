package com.recetalog.view

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.recetalog.R
import com.recetalog.databinding.ActivityMainBinding
import com.recetalog.model.Receta
import com.recetalog.model.RecetaAdapter
import com.recetalog.viewmodel.RecetaViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModel: RecetaViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private var imagenSeleccionadaBitmap: Bitmap? = null
    private var currentEditingReceta: Receta? = null

    private var currentAdapter: RecetaAdapter? = null
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

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }

            val imgPreview = findViewById<ImageView>(R.id.imgRecetaPreview)
            imgPreview?.setImageBitmap(bitmap)
            imagenSeleccionadaBitmap = bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("RecetaLog_prefs", Context.MODE_PRIVATE)

        val darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, 0, 0, 0)
                binding.topBar.setPadding(0, systemBars.top, 0, 0)
                binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
                insets
            }

            setupBottomNavigation()
            loadView(R.layout.view_home)

            viewModel.getAllRecetas().observe(this) { recetas ->
                allRecetas = recetas
                aplicarFiltro()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadView(R.layout.view_home)
                    true
                }
                R.id.navigation_recetas -> {
                    loadView(R.layout.view_recetas)
                    true
                }
                R.id.navigation_acerca_de -> {
                    loadView(R.layout.view_opciones)
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun loadView(layoutResId: Int) {
        binding.lytContenedor.removeAllViews()
        val view = LayoutInflater.from(this).inflate(layoutResId, binding.lytContenedor, false)
        binding.lytContenedor.addView(view)

        if (layoutResId == R.layout.view_opciones) {
            val btnAbt = view.findViewById<View>(R.id.btnAbt)
            btnAbt?.setOnClickListener {
                loadView(R.layout.view_dependencias)
            }

            val btnBor = view.findViewById<View>(R.id.btnBor)
            btnBor?.setOnClickListener {
                showDeleteDataDialog()
            }

            val btnOsc = view.findViewById<View>(R.id.btnOsc)
            btnOsc?.setOnClickListener {
                toggleDarkMode()
            }
        }

        if (layoutResId == R.layout.view_recetas) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.rcvMenuRecetas)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val adapter = RecetaAdapter(
                recetas = allRecetas,
                onItemClick = { receta ->
                    loadRecetaDetalle(receta)
                }
            )
            recyclerView.adapter = adapter
            currentAdapter = adapter

            aplicarFiltro()

            val btnFiltro = view.findViewById<View>(R.id.btnFiltro)
            btnFiltro?.setOnClickListener {
                showFilterDialog()
            }

            val btnAddRec = view.findViewById<View>(R.id.btnAddRec)
            btnAddRec?.setOnClickListener {
                currentEditingReceta = null
                loadView(R.layout.view_add_rec)
            }
        }

        if (layoutResId == R.layout.view_add_rec) {
            setupAddRecetaView(view)
        }

        if (layoutResId == R.layout.view_home) {
            val btnExplore = view.findViewById<View>(R.id.btnExplore)
            btnExplore?.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }
    }

    private fun setupAddRecetaView(view: View) {
        imagenSeleccionadaBitmap = null

        val imgPreview = view.findViewById<ImageView>(R.id.imgRecetaPreview)
        val edtNombre = view.findViewById<TextInputEditText>(R.id.edtNombre)
        val edtIngredientes = view.findViewById<TextInputEditText>(R.id.edtIngredientes)
        val edtPasos = view.findViewById<TextInputEditText>(R.id.edtPasos)
        val txtTitulo = view.findViewById<android.widget.TextView>(R.id.txtTitulo)

        val chipVerdura = view.findViewById<Chip>(R.id.chipVerduraEdit)
        val chipCarne = view.findViewById<Chip>(R.id.chipCarneEdit)
        val chipPescado = view.findViewById<Chip>(R.id.chipPescadoEdit)
        val chipPostre = view.findViewById<Chip>(R.id.chipPostreEdit)
        val chipLactosa = view.findViewById<Chip>(R.id.chipLactosaEdit)
        val chipFruta = view.findViewById<Chip>(R.id.chipFrutaEdit)

        currentEditingReceta?.let { receta ->
            txtTitulo?.text = getString(R.string.editar_receta)
            edtNombre?.setText(receta.nmreceta)
            edtIngredientes?.setText(receta.ingredientes)
            edtPasos?.setText(receta.pasos)

            chipVerdura?.isChecked = receta.isVerdura
            chipCarne?.isChecked = receta.isCarne
            chipPescado?.isChecked = receta.isPescado
            chipPostre?.isChecked = receta.isPostre
            chipLactosa?.isChecked = receta.isLactosa
            chipFruta?.isChecked = receta.isFruta

            if (receta.imagen != null && receta.imagen.isNotEmpty()) {
                imagenSeleccionadaBitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
                imgPreview?.setImageBitmap(imagenSeleccionadaBitmap)
            }
        }

        val btnSeleccionarImagen = view.findViewById<View>(R.id.btnSeleccionarImagen)
        btnSeleccionarImagen?.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        val btnGuardar = view.findViewById<View>(R.id.btnGuardar)
        btnGuardar?.setOnClickListener {
            val nombre = edtNombre?.text?.toString()?.trim()
            val ingredientes = edtIngredientes?.text?.toString()?.trim()
            val pasos = edtPasos?.text?.toString()?.trim()

            if (nombre.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.msg_nombre_vacio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ingredientes.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.msg_ingredientes_vacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pasos.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.msg_pasos_vacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentEditingReceta != null) {
                viewModel.actualizarRecetaEnDB(
                    idreceta = currentEditingReceta!!.idreceta,
                    bitmap = imagenSeleccionadaBitmap,
                    nombre = nombre,
                    isVerdura = chipVerdura?.isChecked ?: false,
                    isCarne = chipCarne?.isChecked ?: false,
                    isPescado = chipPescado?.isChecked ?: false,
                    isPostre = chipPostre?.isChecked ?: false,
                    isLactosa = chipLactosa?.isChecked ?: false,
                    isFruta = chipFruta?.isChecked ?: false,
                    ingredientes = ingredientes,
                    pasos = pasos
                )
                Toast.makeText(this, getString(R.string.msg_receta_actualizada), Toast.LENGTH_SHORT).show()
                currentEditingReceta = null
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            } else {
                viewModel.guardarRecetaEnDB(
                    bitmap = imagenSeleccionadaBitmap,
                    nombre = nombre,
                    isVerdura = chipVerdura?.isChecked ?: false,
                    isCarne = chipCarne?.isChecked ?: false,
                    isPescado = chipPescado?.isChecked ?: false,
                    isPostre = chipPostre?.isChecked ?: false,
                    isLactosa = chipLactosa?.isChecked ?: false,
                    isFruta = chipFruta?.isChecked ?: false,
                    ingredientes = ingredientes,
                    pasos = pasos
                )
                Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
                currentEditingReceta = null
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }

        val btnCancelar = view.findViewById<View>(R.id.btnCancelar)
        btnCancelar?.setOnClickListener {
            currentEditingReceta = null
            binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
        }
    }

    private fun loadRecetaDetalle(receta: Receta) {
        binding.lytContenedor.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.view_rec_detalle, binding.lytContenedor, false)
        binding.lytContenedor.addView(view)

        val imgReceta = view.findViewById<ImageView>(R.id.imgRecetaDetalle)
        val txtNombre = view.findViewById<android.widget.TextView>(R.id.txtNombreRecetaDetalle)
        val txtIngredientes = view.findViewById<android.widget.TextView>(R.id.txtIngredientesDetalle)
        val txtPasos = view.findViewById<android.widget.TextView>(R.id.txtPasosDetalle)

        val chipVerdura = view.findViewById<Chip>(R.id.chipVerduraDetalle)
        val chipCarne = view.findViewById<Chip>(R.id.chipCarneDetalle)
        val chipPescado = view.findViewById<Chip>(R.id.chipPescadoDetalle)
        val chipPostre = view.findViewById<Chip>(R.id.chipPostreDetalle)
        val chipLactosa = view.findViewById<Chip>(R.id.chipLactosaDetalle)
        val chipFruta = view.findViewById<Chip>(R.id.chipFrutaDetalle)

        txtNombre.text = receta.nmreceta
        txtIngredientes.text = receta.ingredientes
        txtPasos.text = receta.pasos

        if (receta.imagen != null && receta.imagen.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
            imgReceta.setImageBitmap(bitmap)
        }

        chipVerdura.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
        chipCarne.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
        chipPescado.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
        chipPostre.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
        chipLactosa.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
        chipFruta.visibility = if (receta.isFruta) View.VISIBLE else View.GONE

        val btnEditar = view.findViewById<View>(R.id.btnEditarReceta)
        btnEditar?.setOnClickListener {
            currentEditingReceta = receta
            loadView(R.layout.view_add_rec)
        }

        val btnEliminar = view.findViewById<View>(R.id.btnEliminarReceta)
        btnEliminar?.setOnClickListener {
            lifecycleScope.launch {
                viewModel.delete(receta)
                Toast.makeText(this@MainActivity, getString(R.string.msg_receta_eliminada), Toast.LENGTH_SHORT).show()
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }
    }

    private fun aplicarFiltro() {
        if (currentAdapter == null) return

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
        currentAdapter?.updateRecetas(recetasFiltradas)
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)

        val chipVerdura = dialogView.findViewById<Chip>(R.id.chipFiltroVerdura)
        val chipCarne = dialogView.findViewById<Chip>(R.id.chipFiltroCarne)
        val chipPescado = dialogView.findViewById<Chip>(R.id.chipFiltroPescado)
        val chipPostre = dialogView.findViewById<Chip>(R.id.chipFiltroPostre)
        val chipLactosa = dialogView.findViewById<Chip>(R.id.chipFiltroLactosa)
        val chipFruta = dialogView.findViewById<Chip>(R.id.chipFiltroFruta)

        chipVerdura.isChecked = filtroActivo.verdura
        chipCarne.isChecked = filtroActivo.carne
        chipPescado.isChecked = filtroActivo.pescado
        chipPostre.isChecked = filtroActivo.postre
        chipLactosa.isChecked = filtroActivo.lactosa
        chipFruta.isChecked = filtroActivo.fruta

        AlertDialog.Builder(this)
            .setTitle(R.string.filtrar_por)
            .setView(dialogView)
            .setPositiveButton(R.string.aplicar_filtro) { _, _ ->
                filtroActivo.verdura = chipVerdura.isChecked
                filtroActivo.carne = chipCarne.isChecked
                filtroActivo.pescado = chipPescado.isChecked
                filtroActivo.postre = chipPostre.isChecked
                filtroActivo.lactosa = chipLactosa.isChecked
                filtroActivo.fruta = chipFruta.isChecked
                aplicarFiltro()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .setNeutralButton(R.string.limpiar_filtro) { _, _ ->
                filtroActivo.reset()
                aplicarFiltro()
            }
            .show()
    }

    private fun showDeleteDataDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.borrar)
            .setMessage(R.string.confirmar_borrado)
            .setPositiveButton(R.string.si) { _, _ ->
                lifecycleScope.launch {
                    allRecetas.forEach { receta ->
                        viewModel.delete(receta)
                    }
                    Toast.makeText(this@MainActivity, getString(R.string.datos_borrados), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun toggleDarkMode() {
        val currentMode = sharedPreferences.getBoolean("dark_mode", false)
        val newMode = !currentMode

        sharedPreferences.edit().putBoolean("dark_mode", newMode).apply()

        if (newMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        recreate()
    }
}
