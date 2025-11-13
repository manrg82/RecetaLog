package com.recetalog.model.conexion

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.recetalog.model.Receta
@Database(entities=[Receta::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun recetaDAO(): RecetaDAO
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receta_database.db3"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}