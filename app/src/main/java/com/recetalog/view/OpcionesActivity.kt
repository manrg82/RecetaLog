package com.recetalog.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.recetalog.R
import com.recetalog.databinding.ActivityOpcionesBinding
import com.recetalog.viewmodel.RecetaViewModel
import kotlinx.coroutines.launch

class OpcionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpcionesBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: RecetaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("RecetaLog_prefs", Context.MODE_PRIVATE)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnOsc.setOnClickListener {
            toggleDarkMode()
        }

        binding.btnIdioma.setOnClickListener {
            showIdiomaDialog()
        }

        binding.btnAbt.setOnClickListener {
            val intent = Intent(this, LicenciasActivity::class.java)
            startActivity(intent)
        }

        binding.btnBor.setOnClickListener {
            showDeleteDataDialog()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun toggleDarkMode() {
        val currentMode = sharedPreferences.getBoolean("dark_mode", false)
        val newMode = !currentMode

        sharedPreferences.edit().putBoolean("dark_mode", newMode).apply()

        if (newMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Toast.makeText(this, getString(R.string.modo_oscuro_activado), Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Toast.makeText(this, getString(R.string.modo_oscuro_desactivado), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showIdiomaDialog() {
        val opciones = arrayOf(
            getString(R.string.idioma_sistema),
            getString(R.string.idioma_espanol),
            getString(R.string.idioma_ingles)
        )

        val currentSetting = sharedPreferences.getString("language", "system") ?: "system"
        val selectedIndex = when (currentSetting) {
            "es" -> 1
            "en" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.seleccionar_idioma)
            .setSingleChoiceItems(opciones, selectedIndex) { dialog, which ->
                val newLanguage = when (which) {
                    1 -> "es"
                    2 -> "en"
                    else -> "system"
                }
                sharedPreferences.edit().putString("language", newLanguage).apply()
                Toast.makeText(this, getString(R.string.idioma_cambiado), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun showDeleteDataDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.borrar)
            .setMessage(R.string.confirmar_borrado)
            .setPositiveButton(R.string.si) { _, _ ->
                lifecycleScope.launch {
                    viewModel.getAllRecetas().value?.forEach { receta ->
                        viewModel.delete(receta)
                    }
                    Toast.makeText(
                        this@OpcionesActivity,
                        getString(R.string.datos_borrados),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}

