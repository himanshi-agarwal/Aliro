package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var email : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var signUpTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        loginButton = findViewById(R.id.login_button)
        signUpTextView = findViewById(R.id.signupLink)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        signUpTextView.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity:: class.java))
        }

        loginButton?.setOnClickListener(){
            startActivity(Intent(this, EmpHomeActivity::class.java))
            finish()

//            val db = DBHelper(this, null)
//
//            val name = email.text.toString()
//            val pass = password.text.toString()
//
//            db.addName(name, pass)
//
//            Toast.makeText(this, name + " User Logged In", Toast.LENGTH_LONG).show()
        }
    }
}