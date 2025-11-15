package com.recetalog

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Test instrumentado que se ejecuta en un dispositivo Android real o emulador.
 * Verifica la correcta vinculación del contexto de la aplicación.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Obtiene el contexto de la app bajo prueba
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Verifica que el nombre del paquete sea correcto
        assertEquals("com.recetalog", appContext.packageName)
    }
}