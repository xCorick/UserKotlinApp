package com.example.proyecto

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Principal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegresar: Button = findViewById(R.id.btnRegresar)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        //Instancia del controlador
        var adapter = ViewPagerAdapter(this)
        viewPager.adapter=adapter

        TabLayoutMediator(tabLayout,viewPager){tab, position ->
            when(position){
                0 -> tab.text="Inicio"
                1 -> tab.text="Clientes"
                2 -> tab.text="Compras"
            }
        }.attach()
        btnRegresar.setOnClickListener {
            finish()
        }


    }
}