package com.recetalog.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.recetalog.databinding.ActivityLicenciasBinding

class LicenciasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicenciasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

