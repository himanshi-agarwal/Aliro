package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginLink : TextView
    private lateinit var backButton : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        loginLink = findViewById(R.id.loginLink)
        backButton = findViewById(R.id.backButton)

        loginLink.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        backButton.setOnClickListener(){
            finish()
        }
    }
}