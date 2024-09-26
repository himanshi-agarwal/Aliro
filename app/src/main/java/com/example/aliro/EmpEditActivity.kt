package com.example.aliro

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class EmpEditActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var pass : EditText
    private lateinit var newpass : EditText
    private lateinit var confirmpass : EditText
    private lateinit var saveButton : Button
    private lateinit var cancelButton : Button
    private lateinit var userProfile : ImageView
    private lateinit var cameraButton: ImageButton
    private lateinit var uploadProgress : ProgressBar
    private var db = Firebase.firestore
    private val CAMERA_PERMISSION_CODE = 101
    private val STORAGE_PERMISSION_CODE = 102
    private val CAMERA_REQUEST_CODE = 103
    private val GALLERY_REQUEST_CODE = 104

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.emp_edit_profile)
        enableEdgeToEdge()

        pass = findViewById(R.id.curr_pass)
        newpass = findViewById(R.id.new_pass)
        confirmpass = findViewById(R.id.confirm_pass)
        saveButton = findViewById(R.id.save)
        cancelButton = findViewById(R.id.cancel)
        cameraButton = findViewById(R.id.camera_icon)
        uploadProgress = findViewById(R.id.progress_bar)
        userProfile = findViewById(R.id.account)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }

        saveButton.setOnClickListener(){
            val password = pass.text.toString()
            val newpassword = newpass.text.toString()
            val confirmpassword = confirmpass.text.toString()

            if(password.isBlank()){
                pass.error = "Filed is Empty"
            }

            if(newpassword.isBlank()){
                newpass.error = "Filed is Empty"
            }

            if(confirmpassword.isBlank()){
                confirmpass.error = "Filed is Empty"
            }

            editProfile(password, newpassword, confirmpassword)
        }

        cancelButton.setOnClickListener(){
            cancelUpdate(pass, newpass, confirmpass)
        }

        cameraButton.setOnClickListener() {
            uploadImage()
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open , R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateUserProfile()
        updateSidebarHeader()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.logs -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LogsActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.profile -> {
                    if (this !is EmpEditActivity) {
                        val intent = Intent(this, EmpEditActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Profile", Toast.LENGTH_SHORT).show()
                    }
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Visitor", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.notification -> {
                    Toast.makeText(applicationContext, "Notifications", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.about -> {
                    Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                R.id.logout -> {
                    Toast.makeText(applicationContext, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun checkPassword(p: String, cp: String) : Boolean{
        return p == cp
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_CODE)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    private fun updateUserProfile() {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val storageReference = Firebase.storage.reference
                val imageReference = storageReference.child("images/${userId}.jpg")

                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(userProfile)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSidebarHeader() {
        val header = navView.getHeaderView(0)
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPreference.getString("userId", null)

        val headerUserName : TextView = header.findViewById(R.id.header_user_name)
        val headerUserType : TextView = header.findViewById(R.id.header_user_type)
        val headerUserProfile : ImageView = header.findViewById(R.id.header_user_profile_picture)

        headerUserName.text = sharedPreference.getString("userName", null)
        headerUserType.text = sharedPreference.getString("userType", null)

        if(checkSession() && userId != null){
            val storageReference = Firebase.storage.reference
            val imageReference = storageReference.child("images/${userId}.jpg")

            imageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(headerUserProfile)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editProfile(password : String, newpassword : String, confirmpassword : String){
        if (checkPassword(newpassword, confirmpassword) && checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)

            val userRef = db.collection("user").document(userId!!)

            db.collection("user")
                .whereEqualTo(FieldPath.documentId(), userId)
                .get()
                .addOnSuccessListener { document ->
                    for (d in document.documents) {
                        val dbPassword = d.getString("password")
                        if (dbPassword != password) {
                            Toast.makeText(this, "Incorrect Current Password!", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                    }

                    userRef.update(
                        mapOf(
                            "username" to userName,
                            "password" to newpassword,
                            "usertype" to "Employee"
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EmpHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error Updating Password", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "User Details Not found", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateEmployeeImage(imageRef: String, userId: String, callback : (Boolean) -> Unit) {
        val userRef = db.collection("user").document(userId)

        db.collection("employees")
            .whereEqualTo("user_ref", userRef)
            .get()
            .addOnSuccessListener{ document ->
                if(!document.isEmpty){
                    for(d in document.documents) {
                        db.collection("employees").document(d.id)
                            .update("Images", imageRef)
                            .addOnSuccessListener {
                                callback(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e("UpdateEmployeeImage", "Failed to update image for user: $userId", e)
                                callback(false)
                            }
                    }
                } else {
                    Log.w("UpdateEmployeeImage", "No document found for user: $userId")
                    callback (false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UpdateEmployeeImage", "Failed to find document for user: $userId", e)
                callback (false)
            }
    }

    private fun cancelUpdate(pass : EditText, newpass : EditText, confirmpass : EditText){
        pass.text.clear()
        newpass.text.clear()
        confirmpass.text.clear()

        Toast.makeText(this, "Changes Discarded", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun uploadImage(){
        checkPermissions()
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select an Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                val selectedImageUri = data.data
                uploadImageToFirebase(selectedImageUri)
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap
                val uri = getImageUriFromBitmap(imageBitmap)
                uploadImageToFirebase(uri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            uploadProgress.visibility = View.VISIBLE

            val storageReference = FirebaseStorage.getInstance().reference

            if(checkSession()){
                val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
                val userID = sharedPreference.getString("userId", null)

                if(userID != null){
                    val imageReference = storageReference.child("images/${userID}.jpg")

                    val uploadTask = imageReference.putFile(imageUri)

                    uploadTask.addOnSuccessListener {

                        updateEmployeeImage(imageReference.toString(), userID) { isSuccess ->
                            if(isSuccess){
                                Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Error Updating Reference", Toast.LENGTH_SHORT).show()
                            }
                        }

                        uploadProgress.visibility = View.GONE
                    }.addOnFailureListener {
                        Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                        uploadProgress.visibility = View.GONE
                    }
                } else{
                    Toast.makeText(this, "Error Loading User Id", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to logout?")

        builder.setTitle("ALERT!")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") { _, _ ->
            val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}