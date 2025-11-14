package com.recetalog.model.conexion

import androidx.lifecycle.LiveData
import com.recetalog.model.Receta

class RecetaRepository(private val recetaDAO: RecetaDAO) {
    suspend fun insert(receta: Receta) {
        recetaDAO.insert(receta)
    }

    fun getAllRecetas(): LiveData<List<Receta>> {
        return recetaDAO.getAllRecetas()
    }

    fun getRecetaById(id: Int): LiveData<Receta> {
        return recetaDAO.getRecetaById(id)
    }

    suspend fun update(receta: Receta) {
        recetaDAO.update(receta)
    }

    suspend fun delete(receta: Receta) {
        recetaDAO.delete(receta)
    }
}