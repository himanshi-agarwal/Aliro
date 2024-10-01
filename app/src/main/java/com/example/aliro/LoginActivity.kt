package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var signUpTextView : TextView

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        loginButton = findViewById(R.id.login_button)
        signUpTextView = findViewById(R.id.signupLink)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        signUpTextView.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity:: class.java))
        }

        loginButton.setOnClickListener(){
            login()
        }
    }

    private fun login(){
        val name = username.text.toString()
        val pass = password.text.toString()

        if(name.isEmpty() || pass.isEmpty()){
            Toast.makeText(this,"Fill all Fields!", Toast.LENGTH_LONG).show()
            return
        } else {
            fetchData(name, pass)
        }
    }

    private fun fetchData(name: String, password: String) {
        db.collection("user")
            .whereEqualTo("username", name)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener {  document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (d in document.documents) {
                        val userId = d.id
                        val userName = d.getString("username")
                        val userType = d.getString("usertype")

                        if (userName != null && userType != null) {
                            saveSession(userName, userType, userId)
                        }

                        Toast.makeText(this, "Welcome, $userName", Toast.LENGTH_SHORT).show()

                        val intent = if (userType == "Employee") {
                            Intent(this, EmpHomeActivity::class.java)
                        } else if (userType == "Visitor") {
                            Intent(this, VisitorHomeActivity::class.java)
                        } else {
                            Intent(this, SecurityHomeActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login Failed! Try Again..", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveSession(userName : String, userType : String, userId : String){
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("userId", userId)
        editor.putString("userName", userName)
        editor.putString("userType", userType)
        editor.putBoolean("loggedIn", true)
        editor.apply()
    }
}