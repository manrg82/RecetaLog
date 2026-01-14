package com.recetalog.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.recetalog.model.Receta
import com.recetalog.model.conexion.RecetaRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * ViewModel para gestionar datos de recetas.
 * Comunica entre la UI (MainActivity) y el repositorio de datos.
 * Maneja operaciones asincrónicas con corrutinas.
 */
class RecetaViewModel(application: Application) : AndroidViewModel(application) {
    // Repositorio para acceder a los datos
    private lateinit var repository: RecetaRepository


    // Inicializa el repositorio y observa los datos
    init{
        try {
            val recetaDAO = com.recetalog.model.conexion.AppDatabase.getDatabase(application).recetaDAO()
            repository = RecetaRepository(recetaDAO)
            android.util.Log.d("RecetaViewModel", "ViewModel inicializado correctamente")
        } catch (e: Exception) {
            android.util.Log.e("RecetaViewModel", "Error al inicializar ViewModel: ${e.message}", e)
            throw e
        }
    }

    /**
     * Guarda una nueva receta en la BD.
     * Convierte el Bitmap a ByteArray para almacenar la imagen.
     */
    fun guardarRecetaEnDB(
        bitmap: Bitmap?,
        nombre: String,
        isVerdura: Boolean,
        isCarne: Boolean,
        isPescado: Boolean,
        isPostre: Boolean,
        isLactosa: Boolean,
        isFruta: Boolean,
        ingredientes: String,
        pasos: String
    ) {
        viewModelScope.launch {
            try {
                // Convierte Bitmap a ByteArray si existe
                val byteArray = bitmap?.let {
                    // Redimensiona la imagen para que sea más pequeña
                    val resizedBitmap = Bitmap.createScaledBitmap(it, 400, 300, true)
                    val stream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                    val compressed = stream.toByteArray()
                    android.util.Log.d("RecetaViewModel", "Imagen comprimida: ${compressed.size} bytes")
                    compressed
                }

                // Crea la entidad Receta con los datos proporcionados
                val receta = Receta(
                    idreceta = 0,
                    nmreceta = nombre,
                    isVerdura = isVerdura,
                    isCarne = isCarne,
                    isPescado = isPescado,
                    isPostre = isPostre,
                    isLactosa = isLactosa,
                    isFruta = isFruta,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    imagen = byteArray
                )

                // Inserta en la BD a través del repositorio
                repository.insert(receta)
                android.util.Log.d("RecetaViewModel", "Receta guardada exitosamente: $nombre")
            } catch (e: Exception) {
                android.util.Log.e("RecetaViewModel", "Error al guardar receta: ${e.message}", e)
            }
        }
    }

    /**
     * Actualiza una receta existente en la BD.
     * Permite cambiar todos los datos incluyendo la imagen.
     */
    fun actualizarRecetaEnDB(
        idreceta: Int,
        bitmap: Bitmap?,
        nombre: String,
        isVerdura: Boolean,
        isCarne: Boolean,
        isPescado: Boolean,
        isPostre: Boolean,
        isLactosa: Boolean,
        isFruta: Boolean,
        ingredientes: String,
        pasos: String
    ) {
        viewModelScope.launch {
            try {
                // Convierte Bitmap a ByteArray si existe
                val byteArray = bitmap?.let {
                    // Redimensiona la imagen para que sea más pequeña
                    val resizedBitmap = Bitmap.createScaledBitmap(it, 400, 300, true)
                    val stream = ByteArrayOutputStream()
                    // Comprime agresivamente a 50% de calidad
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                    val compressed = stream.toByteArray()
                    android.util.Log.d("RecetaViewModel", "Imagen comprimida: ${compressed.size} bytes")
                    compressed
                }

                // Crea la entidad con el ID existente
                val receta = Receta(
                    idreceta = idreceta,
                    nmreceta = nombre,
                    isVerdura = isVerdura,
                    isCarne = isCarne,
                    isPescado = isPescado,
                    isPostre = isPostre,
                    isLactosa = isLactosa,
                    isFruta = isFruta,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    imagen = byteArray
                )

                // Actualiza en la BD a través del repositorio
                repository.update(receta)
                android.util.Log.d("RecetaViewModel", "Receta actualizada exitosamente: $nombre")
            } catch (e: Exception) {
                android.util.Log.e("RecetaViewModel", "Error al actualizar receta: ${e.message}", e)
            }
        }
    }

    // Inserta una receta
    fun insert(receta: Receta) = viewModelScope.launch {
        repository.insert(receta)
    }

    // Actualiza una receta
    fun update(receta: Receta) = viewModelScope.launch{
        repository.update(receta)
    }

    // Elimina una receta
    fun delete(receta: Receta) = viewModelScope.launch{
        repository.delete(receta)
    }

    // Obtiene todas las recetas como LiveData
    fun getAllRecetas(): LiveData<List<Receta>> {
        return repository.getAllRecetas()
    }

    // Obtiene una receta específica por ID
    fun getRecetaById(id: Int): LiveData<Receta> {
        return repository.getRecetaById(id)
    }

}