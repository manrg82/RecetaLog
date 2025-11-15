package com.recetalog.model.conexion

import androidx.lifecycle.LiveData
import com.recetalog.model.Receta

/**
 * Repositorio que actúa como capa de abstracción entre el ViewModel y el DAO.
 * Centraliza la lógica de acceso a datos para las recetas.
 */
class RecetaRepository(private val recetaDAO: RecetaDAO) {
    // Inserta una nueva receta en la base de datos
    suspend fun insert(receta: Receta) {
        recetaDAO.insert(receta)
    }

    // Obtiene todas las recetas
    fun getAllRecetas(): LiveData<List<Receta>> {
        return recetaDAO.getAllRecetas()
    }

    // Obtiene una receta específica por ID
    fun getRecetaById(id: Int): LiveData<Receta> {
        return recetaDAO.getRecetaById(id)
    }

    // Actualiza una receta existente
    suspend fun update(receta: Receta) {
        recetaDAO.update(receta)
    }

    // Elimina una receta
    suspend fun delete(receta: Receta) {
        recetaDAO.delete(receta)
    }
}