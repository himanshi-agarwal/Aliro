package com.example.aliro

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

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

        // Camera icon click listener
        cameraIcon.setOnClickListener {
            openCameraOrGallery()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            saveEmployeeProfile()
        }

        // Cancel button click listener
        cancelButton.setOnClickListener {
            finish() // Close the activity
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
        // Get the data from the EditTexts
        val name = empNameEditText.text.toString()
        val email = empEmailEditText.text.toString()
        val phone = empPhoneEditText.text.toString()
        val role = empRoleEditText.text.toString()
        val company = empCompanyEditText.text.toString()
        val location = empLocationEditText.text.toString()

        // Save the data (you can store it in a database, SharedPreferences, or send to a server)
        // For now, just show a confirmation message
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Profile Saved")
            .setMessage("Employee profile has been saved successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
