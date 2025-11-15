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

/**
 * Actividad principal: gestiona navegación entre vistas, interacción con el ViewModel
 * y operaciones sobre recetas (crear, editar, eliminar, filtrar).
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // ViewModel para operaciones con la base de datos de recetas
    val viewModel: RecetaViewModel by viewModels()
    // Preferencias para guardar configuración del usuario (modo oscuro, etc.)
    private lateinit var sharedPreferences: SharedPreferences

    // Bitmap temporal de la imagen seleccionada al crear/editar receta
    private var imagenSeleccionadaBitmap: Bitmap? = null
    // Receta actualmente en edición (null si se crea una nueva)
    private var currentEditingReceta: Receta? = null

    // Adapter del RecyclerView de recetas
    private var currentAdapter: RecetaAdapter? = null
    // Lista completa de recetas
    private var allRecetas: List<Receta> = emptyList()
    // Estado del filtro actual
    private var filtroActivo = FilterState()

    /**
     * Almacena el estado de los filtros activos.
     * Permite marcar qué categorías están seleccionadas.
     */
    data class FilterState(
        var verdura: Boolean = false,
        var carne: Boolean = false,
        var pescado: Boolean = false,
        var postre: Boolean = false,
        var lactosa: Boolean = false,
        var fruta: Boolean = false
    ) {
        // Comprueba si hay al menos un filtro activo
        fun isActive() = verdura || carne || pescado || postre || lactosa || fruta
        // Desactiva todos los filtros
        fun reset() {
            verdura = false
            carne = false
            pescado = false
            postre = false
            lactosa = false
            fruta = false
        }
    }

    // Lanzador para seleccionar imágenes de la galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Decodifica la URI a Bitmap (compatible con API <28 y >=28)
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
        // Carga preferencias antes de setContentView para aplicar tema
        sharedPreferences = getSharedPreferences("RecetaLog_prefs", Context.MODE_PRIVATE)

        val darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        try {
            // Habilita edge-to-edge y prepara el layout
            enableEdgeToEdge()
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Ajusta padding para las barras de sistema
            ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, 0, 0, 0)
                binding.topBar.setPadding(0, systemBars.top, 0, 0)
                binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
                insets
            }

            setupBottomNavigation()
            loadView(R.layout.view_home)

            // Observa cambios en recetas y aplica filtro
            viewModel.getAllRecetas().observe(this) { recetas ->
                allRecetas = recetas
                aplicarFiltro()
            }


        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error en onCreate: ${e.message}", e)
            e.printStackTrace()
        }

    }

    /**
     * Configura la navegación inferior (bottom navigation).
     * Maneja clics en los tres botones principales de la app.
     */
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
        // Selecciona inicialmente la pestaña Home
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    /**
     * Carga una vista XML dentro del contenedor principal.
     * Ejecuta setup específico según qué vista se está cargando.
     */
    private fun loadView(layoutResId: Int) {
        binding.lytContenedor.removeAllViews()
        val view = LayoutInflater.from(this).inflate(layoutResId, binding.lytContenedor, false)
        binding.lytContenedor.addView(view)

        // Setup para la vista de Opciones/Acerca de
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

        // Setup para la vista de Recetas
        if (layoutResId == R.layout.view_recetas) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.rcvMenuRecetas)
            recyclerView.layoutManager = LinearLayoutManager(this)

            // Crea el adapter con callback para clics en receta
            val adapter = RecetaAdapter(
                recetas = allRecetas,
                onItemClick = { receta ->
                    loadRecetaDetalle(receta)
                }
            )
            recyclerView.adapter = adapter
            currentAdapter = adapter

            aplicarFiltro()

            // Botón para abrir diálogo de filtro
            val btnFiltro = view.findViewById<View>(R.id.btnFiltro)
            btnFiltro?.setOnClickListener {
                showFilterDialog()
            }

            // Botón para crear nueva receta
            val btnAddRec = view.findViewById<View>(R.id.btnAddRec)
            btnAddRec?.setOnClickListener {
                currentEditingReceta = null
                loadView(R.layout.view_add_rec)
            }
        }

        // Setup para la vista de Añadir/Editar Receta
        if (layoutResId == R.layout.view_add_rec) {
            setupAddRecetaView(view)
        }

        // Setup para la vista Home
        if (layoutResId == R.layout.view_home) {
            val btnExplore = view.findViewById<View>(R.id.btnExplore)
            btnExplore?.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }
    }

    /**
     * Configura campos y listeners de la vista para añadir/editar receta.
     * Si currentEditingReceta no es null, carga sus datos para editar.
     */
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

        // Si hay receta en edición, carga sus datos
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

        // Botón para seleccionar imagen de galería
        val btnSeleccionarImagen = view.findViewById<View>(R.id.btnSeleccionarImagen)
        btnSeleccionarImagen?.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Botón Guardar: inserta o actualiza receta
        val btnGuardar = view.findViewById<View>(R.id.btnGuardar)
        btnGuardar?.setOnClickListener {
            val nombre = edtNombre?.text?.toString()?.trim()
            val ingredientes = edtIngredientes?.text?.toString()?.trim()
            val pasos = edtPasos?.text?.toString()?.trim()

            // Validaciones de campos obligatorios
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

            try {
                // Actualizar si es edición, guardar si es creación
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
                }

                // Cambia de vista después de un delay para permitir que se complete la operación
                currentEditingReceta = null
                binding.lytContenedor.postDelayed({
                    binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
                }, 800)

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error al guardar receta: ${e.message}", e)
                Toast.makeText(this, getString(R.string.msg_error_guardar), Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Cancelar
        val btnCancelar = view.findViewById<View>(R.id.btnCancelar)
        btnCancelar?.setOnClickListener {
            currentEditingReceta = null
            binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
        }
    }

    /**
     * Muestra el detalle de una receta con opciones para editar y eliminar.
     */
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

        // Rellena los campos con datos de la receta
        txtNombre.text = receta.nmreceta
        txtIngredientes.text = receta.ingredientes
        txtPasos.text = receta.pasos

        if (receta.imagen != null && receta.imagen.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(receta.imagen, 0, receta.imagen.size)
            imgReceta.setImageBitmap(bitmap)
        }

        // Muestra u oculta etiquetas según la receta
        chipVerdura.visibility = if (receta.isVerdura) View.VISIBLE else View.GONE
        chipCarne.visibility = if (receta.isCarne) View.VISIBLE else View.GONE
        chipPescado.visibility = if (receta.isPescado) View.VISIBLE else View.GONE
        chipPostre.visibility = if (receta.isPostre) View.VISIBLE else View.GONE
        chipLactosa.visibility = if (receta.isLactosa) View.VISIBLE else View.GONE
        chipFruta.visibility = if (receta.isFruta) View.VISIBLE else View.GONE

        // Botón Editar
        val btnEditar = view.findViewById<View>(R.id.btnEditarReceta)
        btnEditar?.setOnClickListener {
            currentEditingReceta = receta
            loadView(R.layout.view_add_rec)
        }

        // Botón Eliminar
        val btnEliminar = view.findViewById<View>(R.id.btnEliminarReceta)
        btnEliminar?.setOnClickListener {
            lifecycleScope.launch {
                viewModel.delete(receta)
                Toast.makeText(this@MainActivity, getString(R.string.msg_receta_eliminada), Toast.LENGTH_SHORT).show()
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }
    }

    /**
     * Aplica el filtro activo sobre la lista de recetas.
     * Usa OR lógico: una receta se muestra si cumple CUALQUIERA de los filtros activos.
     */
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

    /**
     * Muestra un diálogo para seleccionar filtros.
     */
    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)

        val chipVerdura = dialogView.findViewById<Chip>(R.id.chipFiltroVerdura)
        val chipCarne = dialogView.findViewById<Chip>(R.id.chipFiltroCarne)
        val chipPescado = dialogView.findViewById<Chip>(R.id.chipFiltroPescado)
        val chipPostre = dialogView.findViewById<Chip>(R.id.chipFiltroPostre)
        val chipLactosa = dialogView.findViewById<Chip>(R.id.chipFiltroLactosa)
        val chipFruta = dialogView.findViewById<Chip>(R.id.chipFiltroFruta)

        // Inicializa estado del diálogo con filtros activos
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

    /**
     * Muestra diálogo de confirmación para borrar todas las recetas.
     */
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

    /**
     * Alterna el modo oscuro y guarda la preferencia.
     */
    private fun toggleDarkMode() {
        val currentMode = sharedPreferences.getBoolean("dark_mode", false)
        val newMode = !currentMode

        sharedPreferences.edit().putBoolean("dark_mode", newMode).apply()

        if (newMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Recrea la actividad para aplicar el cambio de tema
        recreate()
    }
}
