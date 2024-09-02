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
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var username : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var signUpTextView : TextView
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        loginButton = findViewById(R.id.login_button)
        signUpTextView = findViewById(R.id.signupLink)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

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
            sendData(name, pass)
        }
    }

    private fun sendData(name: String, password: String) {
        val newData = DataModels(name, password, "random@gmail.com", 9610078190, "Visitor")
        apiService.postData(newData).enqueue(object : Callback<DataModels> {
            override fun onResponse(
                call: Call<DataModels>,
                response: Response<DataModels>
            ) {
                if (response.isSuccessful) {
                    val returnedData = response.body()
                    Log.d("MainActivity", "Data posted: $returnedData")
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DataModels>, t: Throwable) {
                Log.e("MainActivity", "Error posting data", t)
                Toast.makeText(this@LoginActivity, "An error occurred!", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchData(name: String, password: String) {
        apiService.getData().enqueue(object : Callback<List<DataModels>> {
            override fun onResponse(
                call: Call<List<DataModels>>,
                response: Response<List<DataModels>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("MainActivity", "Data fetched: $data Data mil gaya")
                }
            }

            override fun onFailure(call: Call<List<DataModels>>, t: Throwable) {
                Log.e("MainActivity", "Error fetching data", t)
            }
        })
    }
}