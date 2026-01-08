package com.recetalog.view

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.recetalog.R
import com.recetalog.databinding.ActivityAddRecetaBinding
import com.recetalog.viewmodel.RecetaViewModel

class AddRecetaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRecetaBinding
    private val viewModel: RecetaViewModel by viewModels()

    private var imagenSeleccionadaBitmap: Bitmap? = null
    private var recetaIdEdicion: Int? = null
    private var imagenOriginal: ByteArray? = null

    companion object {
        const val EXTRA_RECETA_ID = "extra_receta_id"
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
            binding.imgRecetaPreview.setImageBitmap(bitmap)
            imagenSeleccionadaBitmap = bitmap
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recetaIdEdicion = intent.getIntExtra(EXTRA_RECETA_ID, -1).takeIf { it != -1 }

        setupListeners()

        recetaIdEdicion?.let { id ->
            binding.txtTitulo.text = getString(R.string.editar_receta)
            loadRecetaData(id)
        }
    }

    private fun loadRecetaData(id: Int) {
        viewModel.getRecetaById(id).observe(this) { receta ->
            receta?.let {
                binding.edtNombre.setText(it.nmreceta)
                binding.edtIngredientes.setText(it.ingredientes)
                binding.edtPasos.setText(it.pasos)

                binding.chipVerduraEdit.isChecked = it.isVerdura
                binding.chipCarneEdit.isChecked = it.isCarne
                binding.chipPescadoEdit.isChecked = it.isPescado
                binding.chipPostreEdit.isChecked = it.isPostre
                binding.chipLactosaEdit.isChecked = it.isLactosa
                binding.chipFrutaEdit.isChecked = it.isFruta

                if (it.imagen != null && it.imagen.isNotEmpty()) {
                    imagenOriginal = it.imagen
                    val bitmap = BitmapFactory.decodeByteArray(it.imagen, 0, it.imagen.size)
                    binding.imgRecetaPreview.setImageBitmap(bitmap)
                    imagenSeleccionadaBitmap = bitmap
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnSeleccionarImagen.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            guardarReceta()
        }

        binding.btnCancelar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun guardarReceta() {
        val nombre = binding.edtNombre.text?.toString()?.trim()
        val ingredientes = binding.edtIngredientes.text?.toString()?.trim()
        val pasos = binding.edtPasos.text?.toString()?.trim()

        if (nombre.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.msg_nombre_vacio), Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredientes.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.msg_ingredientes_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        if (pasos.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.msg_pasos_vacios), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (recetaIdEdicion != null) {
                viewModel.actualizarRecetaEnDB(
                    idreceta = recetaIdEdicion!!,
                    bitmap = imagenSeleccionadaBitmap,
                    nombre = nombre,
                    isVerdura = binding.chipVerduraEdit.isChecked,
                    isCarne = binding.chipCarneEdit.isChecked,
                    isPescado = binding.chipPescadoEdit.isChecked,
                    isPostre = binding.chipPostreEdit.isChecked,
                    isLactosa = binding.chipLactosaEdit.isChecked,
                    isFruta = binding.chipFrutaEdit.isChecked,
                    ingredientes = ingredientes,
                    pasos = pasos
                )
                Toast.makeText(this, getString(R.string.msg_receta_actualizada), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.guardarRecetaEnDB(
                    bitmap = imagenSeleccionadaBitmap,
                    nombre = nombre,
                    isVerdura = binding.chipVerduraEdit.isChecked,
                    isCarne = binding.chipCarneEdit.isChecked,
                    isPescado = binding.chipPescadoEdit.isChecked,
                    isPostre = binding.chipPostreEdit.isChecked,
                    isLactosa = binding.chipLactosaEdit.isChecked,
                    isFruta = binding.chipFrutaEdit.isChecked,
                    ingredientes = ingredientes,
                    pasos = pasos
                )
                Toast.makeText(this, getString(R.string.msg_receta_guardada), Toast.LENGTH_SHORT).show()
            }

            setResult(Activity.RESULT_OK)
            binding.root.postDelayed({
                finish()
            }, 500)

        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.msg_error_guardar), Toast.LENGTH_SHORT).show()
        }
    }
}

