package com.example.aliro

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class VisitorEditActivity : AppCompatActivity() {

    private lateinit var visitorName: EditText
    private lateinit var visitorEmail: EditText
    private lateinit var visitorPhone: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visitor_edit_profile)

        visitorName = findViewById(R.id.visitor_name)
        visitorEmail = findViewById(R.id.visitor_email)
        visitorPhone = findViewById(R.id.visitor_phone)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)

        saveButton.setOnClickListener {
            saveVisitorInfo()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveVisitorInfo() {
        val updatedName = visitorName.text.toString()
        val updatedEmail = visitorEmail.text.toString()
        val updatedPhone = visitorPhone.text.toString()

        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val editor = sharedPreference.edit()

            val db = Firebase.firestore
            val userDocRef = db.collection("user").document(userId!!)

            userDocRef.update(
                mapOf(
                    "name" to updatedName,
                    "email" to updatedEmail,
                    "phone_no" to updatedPhone
                )
            )
                .addOnSuccessListener {
                    editor.putString("userName", updatedName)
                    editor.apply()
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {e ->
                    Toast.makeText(this, "Error in Saving Profile", Toast.LENGTH_LONG).show()
                    Log.e("Firestore Error", e.message.toString())
                }
        }
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }
}
