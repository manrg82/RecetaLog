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
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

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

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }
        val width = (drawable.intrinsicWidth.takeIf { it > 0 } ?: 1)
        val height = (drawable.intrinsicHeight.takeIf { it > 0 } ?: 1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getBitmapFromResourceName(name: String): Bitmap? {
        val resIdDrawable = resources.getIdentifier(name, "drawable", packageName)
        val resIdMipmap = resources.getIdentifier(name, "mipmap", packageName)
        val resId = if (resIdDrawable != 0) resIdDrawable else if (resIdMipmap != 0) resIdMipmap else 0
        if (resId == 0) return null
        val drawable = ContextCompat.getDrawable(this, resId) ?: return null
        return drawableToBitmap(drawable)
    }

    private fun insertarDatosIniciales() {
        viewModel.getAllRecetas().observe(this) { recetas ->
            if (recetas.isEmpty()) {
                lifecycleScope.launch {
                    val bmpEnsalada = getBitmapFromResourceName("ensalada")
                    viewModel.guardarRecetaEnDB(
                        bitmap = bmpEnsalada,
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

                    val bmpSalmon = getBitmapFromResourceName("salmon")
                    viewModel.guardarRecetaEnDB(
                        bitmap = bmpSalmon,
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

                    val bmpTarta = getBitmapFromResourceName("tartamanzana")
                    viewModel.guardarRecetaEnDB(
                        bitmap = bmpTarta,
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

                    val bmpPollo = getBitmapFromResourceName("curry")
                    viewModel.guardarRecetaEnDB(
                        bitmap = bmpPollo,
                        nombre = "Pollo al Curry",
                        isVerdura = true,
                        isCarne = true,
                        isPescado = false,
                        isPostre = false,
                        isLactosa = true,
                        isFruta = false,
                        ingredientes = "• 500g pollo\n• 2 cebollas\n• 2 tomates\n• 2 cucharadas curry\n• Leche de coco\n• Aceite\n• Sal y pimienta",
                        pasos = "1. Pochar la cebolla y el ajo.\n2. Añadir el pollo y dorar.\n3. Agregar tomate y curry.\n4. Verter leche de coco y cocinar 20 minutos.\n5. Servir con arroz."
                    )
                }
            }
        }
    }
}
