package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginLink : TextView
    private lateinit var signupButton : Button
    private lateinit var userName : EditText
    private lateinit var phoneNo : EditText
    private lateinit var password : EditText
    private lateinit var confirmPassword : EditText
    private lateinit var email : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        loginLink = findViewById(R.id.loginLink)
        signupButton = findViewById(R.id.signup_button)
        userName = findViewById(R.id.username)
        phoneNo = findViewById(R.id.phone_no)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)

        loginLink.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        signupButton.setOnClickListener(){
            val db = DBHelper(this, null)

            val name = userName.text.toString()
            val phoneStr = phoneNo.text.toString()
            val email = email.text.toString()
            val password = password.text.toString()
            val confirmPassword = confirmPassword.text.toString()
            val userType = "Visitor"

            if (name.isBlank() || phoneStr.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPassword(password, confirmPassword)){
                db.addUser(name, password, email, phoneStr, userType)
                Toast.makeText(applicationContext, "Registration Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else{
                Toast.makeText(applicationContext, "Password Do not Match", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPassword(p: String, cp: String) : Boolean{
        return p == cp
    }
}