package com.recetalog.model.conexion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

/**
 * Converters para que Room pueda serializar/deserializar Bitmap a/desde ByteArray.
 * Permite almacenar imágenes directamente en la base de datos.
 */
class Converters {
    // Convierte Bitmap a ByteArray para guardar en la BD
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    // Convierte ByteArray a Bitmap para mostrar en la UI
    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return byteArray?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
}