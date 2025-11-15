package com.recetalog.model.conexion

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.recetalog.model.Receta

/**
 * DAO (Data Access Object) para acceder a la tabla Receta en la base de datos.
 * Define operaciones CRUD: insertar, leer, actualizar y eliminar recetas.
 */
@Dao
interface RecetaDAO {
    // Inserta una nueva receta
    @Insert
    suspend fun insert(receta: Receta)

    // Obtiene todas las recetas como LiveData para observar cambios
    @Query("SELECT * FROM Receta")
    fun getAllRecetas(): LiveData<List<Receta>>

    // Obtiene una receta específica por ID
    @Query("SELECT * FROM Receta WHERE idreceta = :id")
    fun getRecetaById(id: Int): LiveData<Receta>

    // Actualiza una receta existente
    @Update
    suspend fun update(receta: Receta)

    // Elimina una receta
    @Delete
    suspend fun delete(receta: Receta)
}