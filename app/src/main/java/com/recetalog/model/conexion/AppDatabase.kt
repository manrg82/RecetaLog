package com.recetalog.model.conexion

import androidx.room.Database
import androidx.room.RoomDatabase
import com.recetalog.model.Receta

/**
 * Base de datos Room que gestiona la tabla Receta.
 * Utiliza el patrón Singleton para garantizar una única instancia.
 */
@Database(entities=[Receta::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    // Proporciona acceso al DAO de Receta
    abstract fun recetaDAO(): RecetaDAO

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Obtiene o crea la instancia de la base de datos de forma segura
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receta_database.db3"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

}