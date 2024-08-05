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
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var signUpTextView : TextView

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

        loginButton?.setOnClickListener(){
            login()
        }
    }

    private fun login(){
        val db = DBHelper(this, null)

        val name = username.text.toString()
        val pass = password.text.toString()

        if(name.isNullOrEmpty() || pass.isNullOrEmpty()){
            Toast.makeText(this,"Fill all Fields!", Toast.LENGTH_LONG).show()
            return
        }

        val user: ArrayList<String> = db.loginUser(name, pass)
        if(user.isEmpty()){
            Toast.makeText(this,"Invalid Credentials", Toast.LENGTH_LONG).show()
        }
        else{
            val usertype = user[3]
            if(usertype == "Visitor"){
                val intent = Intent(this, VisitorHomeActivity::class.java)
                intent.putStringArrayListExtra("user", user)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, EmpHomeActivity::class.java)
                intent.putStringArrayListExtra("employee", user)
                startActivity(intent)
                finish()
            }
        }
    }
}