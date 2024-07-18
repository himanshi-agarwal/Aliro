package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash)

        val button = findViewById<Button>(R.id.proceed_button)
        button?.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}