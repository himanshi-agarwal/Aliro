package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Parking : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parking)


        val button: Button = findViewById(R.id.button)


        button.setOnClickListener {

            val intent = Intent(this, Parking_register::class.java)
            startActivity(intent)
        }
    }
}
