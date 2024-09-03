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
        db.collection("users")
            .whereEqualTo("name", name)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { document ->
                Log.d("data", document)
                if(document.isEmpty){
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (d in document) {
                        val userName = d.getString("name")
                        val userType = d.getString("user_type")
                        val intent:Intent
                        Toast.makeText(this, "Welcome, $userName", Toast.LENGTH_SHORT).show()

                        if (userType == "Employee"){
                            intent = Intent (this, EmpHomeActivity::class.java)
                        }
                        else{
                            intent = Intent (this, VisitorHomeActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Login Failed! Try Again..", Toast.LENGTH_SHORT).show()
            }
    }
}