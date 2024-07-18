package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton?.setOnClickListener(){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}