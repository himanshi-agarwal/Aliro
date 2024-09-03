package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginLink : TextView
    private lateinit var signupButton : Button
    private lateinit var userName : EditText
    private lateinit var phoneNo : EditText
    private lateinit var password : EditText
    private lateinit var confirmPassword : EditText
    private lateinit var email : EditText

    private var db = Firebase.firestore

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
            val name = userName.text.toString().trim()
            val phoneStr = phoneNo.text.toString().trim()
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()
            val confirmPassword = confirmPassword.text.toString().trim()
            val userType = "Visitor"

            if (name.isBlank() || phoneStr.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(checkPassword(password, confirmPassword)){
                insertData(name, phoneStr, email, password, userType)
            } else {
                Toast.makeText(applicationContext, "Password Do not Match", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun insertData(name: String, phone_no: String, email: String, password: String, usertype: String){
        val userMap = hashMapOf(
            "name" to name,
            "phone_no" to phone_no,
            "email" to email,
            "password" to password,
            "user_type" to usertype
        )

        db.collection("user").document().set(userMap)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Registration Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkPassword(p: String, cp: String) : Boolean{
        return p == cp
    }
}