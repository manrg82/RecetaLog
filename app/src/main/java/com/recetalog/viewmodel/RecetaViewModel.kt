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

class RecetaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecetaRepository
    val data: LiveData<List<Receta>>

    private val _guardadoExitoso = MutableLiveData<Boolean>()
    val guardadoExitoso: LiveData<Boolean> get() = _guardadoExitoso

    init{
        val recetaDAO = com.recetalog.model.conexion.AppDatabase.getDatabase(application).recetaDAO()
        repository = RecetaRepository(recetaDAO)
        data = repository.getAllRecetas()
    }

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
                val byteArray = bitmap?.let {
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    stream.toByteArray()
                }

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

                repository.insert(receta)

                _guardadoExitoso.postValue(true)
            } catch (e: Exception) {
                _guardadoExitoso.postValue(false)
            }
        }
    }

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
                val byteArray = bitmap?.let {
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    stream.toByteArray()
                }

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

                repository.update(receta)

                _guardadoExitoso.postValue(true)
            } catch (e: Exception) {
                _guardadoExitoso.postValue(false)
            }
        }
    }

    fun insert(receta: Receta) = viewModelScope.launch {
        repository.insert(receta)
    }

    fun update(receta: Receta) = viewModelScope.launch{
        repository.update(receta)
    }

    fun delete(receta: Receta) = viewModelScope.launch{
        repository.delete(receta)
    }

    fun getAllRecetas(): LiveData<List<Receta>> {
        return repository.getAllRecetas()
    }

    fun getRecetaById(id: Int): LiveData<Receta> {
        return repository.getRecetaById(id)
    }

}