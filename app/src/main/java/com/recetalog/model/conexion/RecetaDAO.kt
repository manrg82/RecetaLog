package com.recetalog.model.conexion

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.recetalog.model.Receta

@Dao
interface RecetaDAO {
    @Insert
    suspend fun insert(receta: Receta)

    @Query("SELECT * FROM Receta")
    fun getAllRecetas(): LiveData<List<Receta>>

    @Query("SELECT * FROM Receta WHERE idreceta = :id")
    fun getRecetaById(id: Int): LiveData<Receta>

    @Update
    suspend fun update(receta: Receta)

    @Delete
    suspend fun delete(receta: Receta)
}