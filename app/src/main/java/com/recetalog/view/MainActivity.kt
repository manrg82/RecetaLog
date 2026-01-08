package com.recetalog.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.recetalog.R
import com.recetalog.databinding.ActivityMainBinding
import com.recetalog.model.Receta
import com.recetalog.viewmodel.RecetaViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Actividad principal: pantalla de inicio con navegación a Recetas y Opciones.
 * También maneja la configuración de idioma y modo oscuro.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: RecetaViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("RecetaLog_prefs", Context.MODE_PRIVATE)
        applyLanguageConfig()

        val darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

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
        insertarDatosIniciales()
    }

    private fun applyLanguageConfig() {
        val languageSetting = sharedPreferences.getString("language", "system") ?: "system"

        if (languageSetting != "system") {
            val locale = Locale(languageSetting)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_recetas -> {
                    val intent = Intent(this, RecetasActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_acerca_de -> {
                    val intent = Intent(this, OpcionesActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.navigation_home

        binding.btnExplore?.setOnClickListener {
            val intent = Intent(this, RecetasActivity::class.java)
            startActivity(intent)
        }
    }

    private fun insertarDatosIniciales() {
        viewModel.getAllRecetas().observe(this) { recetas ->
            if (recetas.isEmpty()) {
                lifecycleScope.launch {
                    viewModel.guardarRecetaEnDB(
                        bitmap = null,
                        nombre = "Ensalada César",
                        isVerdura = true,
                        isCarne = false,
                        isPescado = false,
                        isPostre = false,
                        isLactosa = true,
                        isFruta = false,
                        ingredientes = "• 1 lechuga romana\n• 100g queso parmesano\n• Crutones\n• Aderezo César\n• Pollo a la plancha (opcional)",
                        pasos = "1. Lavar y cortar la lechuga en trozos.\n2. Añadir los crutones.\n3. Rallar el queso parmesano por encima.\n4. Agregar el aderezo César.\n5. Mezclar bien y servir."
                    )

                    viewModel.guardarRecetaEnDB(
                        bitmap = null,
                        nombre = "Salmón al Horno",
                        isVerdura = false,
                        isCarne = false,
                        isPescado = true,
                        isPostre = false,
                        isLactosa = false,
                        isFruta = false,
                        ingredientes = "• 4 filetes de salmón\n• 2 limones\n• Aceite de oliva\n• Sal y pimienta\n• Eneldo fresco\n• 2 dientes de ajo",
                        pasos = "1. Precalentar el horno a 200°C.\n2. Colocar el salmón en una bandeja.\n3. Sazonar con sal, pimienta y ajo.\n4. Rociar con aceite y jugo de limón.\n5. Hornear 15-20 minutos.\n6. Decorar con eneldo y servir."
                    )

                    viewModel.guardarRecetaEnDB(
                        bitmap = null,
                        nombre = "Tarta de Manzana",
                        isVerdura = false,
                        isCarne = false,
                        isPescado = false,
                        isPostre = true,
                        isLactosa = true,
                        isFruta = true,
                        ingredientes = "• 4 manzanas\n• 200g harina\n• 100g mantequilla\n• 100g azúcar\n• 2 huevos\n• Canela en polvo",
                        pasos = "1. Mezclar harina, mantequilla y azúcar para la masa.\n2. Forrar un molde con la masa.\n3. Pelar y cortar las manzanas en láminas.\n4. Colocar las manzanas sobre la masa.\n5. Espolvorear con canela y azúcar.\n6. Hornear a 180°C por 35 minutos."
                    )

                    viewModel.guardarRecetaEnDB(
                        bitmap = null,
                        nombre = "Pollo al Curry",
                        isVerdura = true,
                        isCarne = true,
                        isPescado = false,
                        isPostre = false,
                        isLactosa = true,
                        isFruta = false,
                        ingredientes = "• 500g pechuga de pollo\n• 1 cebolla\n• 400ml leche de coco\n• 2 cucharadas de curry\n• 1 pimiento rojo\n• Arroz basmati",
                        pasos = "1. Cortar el pollo en cubos.\n2. Sofreír la cebolla y el pimiento.\n3. Añadir el pollo y dorar.\n4. Agregar el curry y mezclar.\n5. Verter la leche de coco.\n6. Cocinar 20 min a fuego lento.\n7. Servir con arroz."
                    )

                    viewModel.guardarRecetaEnDB(
                        bitmap = null,
                        nombre = "Batido Tropical",
                        isVerdura = false,
                        isCarne = false,
                        isPescado = false,
                        isPostre = false,
                        isLactosa = true,
                        isFruta = true,
                        ingredientes = "• 1 plátano\n• 1 mango\n• 200ml leche\n• 100g yogur natural\n• Miel al gusto\n• Hielo",
                        pasos = "1. Pelar y trocear las frutas.\n2. Añadir todos los ingredientes a la licuadora.\n3. Licuar hasta obtener consistencia suave.\n4. Servir frío."
                    )
                }
            }
        }
    }
}

