package com.recetalog.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.recetalog.R
import com.recetalog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            binding.topBar.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)

            insets
        }
        setupBottomNavigation()
        loadView(R.layout.view_home)//aqui cargo la vista de home por defecto en la app con el metodo loadView
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadView(R.layout.view_home)
                    true
                }
                R.id.navigation_recetas -> {
                    loadView(R.layout.view_recipes)
                    true
                }
                R.id.navigation_acerca_de -> {
                    loadView(R.layout.view_profile)
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun loadView(layoutResId: Int) {
        binding.lytContenedor.removeAllViews()
        val view = LayoutInflater.from(this).inflate(layoutResId, binding.lytContenedor, false)
        binding.lytContenedor.addView(view)
        if (layoutResId == R.layout.view_profile) {
            val btnAbt = view.findViewById<View>(R.id.btnOsc)
            btnAbt?.setOnClickListener {
                loadView(R.layout.view_dependency)
            }
        }
            val btnExplore = view.findViewById<View>(R.id.btnExplore)
            btnExplore?.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.navigation_recetas
            }
        }
    }
