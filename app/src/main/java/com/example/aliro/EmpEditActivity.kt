package com.example.aliro

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class EmpEditActivity : AppCompatActivity() {

    private lateinit var cameraIcon: ImageView
    private lateinit var profileImageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var empNameEditText: EditText
    private lateinit var empEmailEditText: EditText
    private lateinit var empPhoneEditText: EditText
    private lateinit var empRoleEditText: EditText
    private lateinit var empCompanyEditText: EditText
    private lateinit var empLocationEditText: EditText

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emp_edit_profile)

        cameraIcon = findViewById(R.id.camera_icon)
        profileImageView = findViewById(R.id.account)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        empNameEditText = findViewById(R.id.emp_name)
        empEmailEditText = findViewById(R.id.emp_email)
        empPhoneEditText = findViewById(R.id.emp_phone)
        empRoleEditText = findViewById(R.id.emp_role)
        empCompanyEditText = findViewById(R.id.emp_company)
        empLocationEditText = findViewById(R.id.emp_location)

        cameraIcon.setOnClickListener {
            openCameraOrGallery()
        }

        saveButton.setOnClickListener {
            saveEmployeeProfile()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun openCameraOrGallery() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> openCamera()
                "Choose from Gallery" -> openGallery()
                "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    profileImageView.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri: Uri? = data?.data
                    profileImageView.setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun saveEmployeeProfile() {
        val updatedName = empNameEditText.text.toString()
        val updatedEmail = empEmailEditText.text.toString()
        val updatedPhone = empPhoneEditText.text.toString()

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


        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile Saved")
            .setMessage("Employee profile has been saved successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }
}
