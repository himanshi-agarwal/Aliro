package com.example.aliro

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AdminEditActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var cameraIcon: ImageView
    private lateinit var profileImageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var empNameEditText: EditText
    private lateinit var empEmailEditText: EditText
    private lateinit var empPhoneEditText: EditText
    private lateinit var empRoleEditText: EditText
    private lateinit var empCompanyEditText: EditText

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emp_home)

        cameraIcon = findViewById(R.id.camera_icon)
        profileImageView = findViewById(R.id.account)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        empNameEditText = findViewById(R.id.emp_name)
        empEmailEditText = findViewById(R.id.emp_email)
        empPhoneEditText = findViewById(R.id.emp_phone)
        empRoleEditText = findViewById(R.id.emp_role)
        empCompanyEditText = findViewById(R.id.emp_company)

        cameraIcon.setOnClickListener {
            openCameraOrGallery()
        }

        saveButton.setOnClickListener {
            saveEmployeeProfile()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.home -> {
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.timings -> Toast.makeText(applicationContext, "Timings", Toast.LENGTH_SHORT).show()

                R.id.profile -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()

                R.id.pre_register -> Toast.makeText(applicationContext, "Pre-Register", Toast.LENGTH_SHORT).show()

                R.id.notification -> {
                    Toast.makeText(applicationContext, "Notifications", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }

                R.id.about -> {
                    Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }

                R.id.logout -> {
                    Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            return super.onBackPressed()
        }
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