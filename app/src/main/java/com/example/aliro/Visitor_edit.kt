package com.example.aliro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView

class Visitor_edit : AppCompatActivity() {

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
            // Handle saving the visitor information
            saveVisitorInfo()
        }

        cancelButton.setOnClickListener {
            // Handle cancel operation, possibly finish the activity
            finish()
        }

        setupNavigationView()
    }

    private fun saveVisitorInfo() {
        // Save the information from the EditTexts
        val name = visitorName.text.toString()
        val email = visitorEmail.text.toString()
        val phone = visitorPhone.text.toString()

        // Add logic to save this data or send it to a server
    }

    private fun setupNavigationView() {
        val navigationView: NavigationView = findViewById(R.id.navbar)
        // Setup navigation view item selection listener here
    }
}
