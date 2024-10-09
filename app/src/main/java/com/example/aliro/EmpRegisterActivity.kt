package com.example.aliro

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.Calendar

class EmpRegisterActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar
    private lateinit var navView : NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var name: EditText
    private lateinit var phone_no: EditText
    private lateinit var email: EditText
    private lateinit var purpose: EditText
    private lateinit var visitDateButton: Button
    private lateinit var visitDate: TextView
    private lateinit var selectImageLayout: LinearLayout
    private lateinit var uploadPhoto: ImageView
    private lateinit var clickPhoto: ImageView
    private lateinit var imageLayout: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var imageName: TextView
    private lateinit var registerButton: Button
    private val CAMERA_PERMISSION_CODE = 101
    private val STORAGE_PERMISSION_CODE = 102
    private val CAMERA_REQUEST_CODE = 103
    private val GALLERY_REQUEST_CODE = 104
    var imageFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.emp_register)

        name = findViewById(R.id.fullname)
        phone_no = findViewById(R.id.phone_no)
        email = findViewById(R.id.email)
        purpose = findViewById(R.id.purpose)
        visitDateButton = findViewById(R.id.visitDateButton)
        visitDate = findViewById(R.id.visitDate)
        selectImageLayout = findViewById(R.id.selectImageLayout)
        uploadPhoto = findViewById(R.id.uploadPhoto)
        clickPhoto = findViewById(R.id.clickPhoto)
        imageLayout = findViewById(R.id.uploadedImageLayout)
        imageView = findViewById(R.id.uploadedImage)
        imageName = findViewById(R.id.imageName)
        registerButton = findViewById(R.id.register_button)

        navView = findViewById(R.id.navbar)
        drawerLayout = findViewById(R.id.drawer_layout)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateSidebarHeader()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.logs -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile -> {
                    Toast.makeText(applicationContext, "Edit Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpEditActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    if (this !is EmpRegisterActivity) {
                        val intent = Intent(this, EmpRegisterActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Registration Page", Toast.LENGTH_SHORT).show()
                    }
                }

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

        visitDateButton.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { view, year, monthOfYear, dayOfMonth ->
                    Log.i("Date", dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    visitDateButton.visibility = View.GONE
                    visitDate.visibility = View.VISIBLE
                    visitDate.text = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                }, year, month, day
            )
            datePickerDialog.show()
        }

        uploadPhoto.setOnClickListener(){
            openGallery()
        }

        clickPhoto.setOnClickListener(){
            openCamera()
        }

        registerButton.setOnClickListener(){
            checkDetails()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun checkGalleryPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), STORAGE_PERMISSION_CODE)
        }
    }

    private fun checkDetails(): Boolean{
        val visitorName = name.text.toString()
        val visitorPhoneNo = phone_no.text.toString()
        val visitorEmail = email.text.toString()
        val purpose = purpose.text.toString()
        val visitDate = visitDate.text.toString()

        if(visitorName.isBlank() || visitorPhoneNo.isBlank() || visitorEmail.isBlank()){
            Toast.makeText(this, "Visitor Details Incomplete", Toast.LENGTH_SHORT).show()
            return false
        }

        if(purpose.isBlank()){
            Toast.makeText(this, "Please Provide a Purpose", Toast.LENGTH_SHORT).show()
            return false
        }

        if(visitDate.isBlank()){
            Toast.makeText(this, "Please Select a Date", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!imageFlag){
            Toast.makeText(this, "Please Select a Image", Toast.LENGTH_SHORT).show()
            return false
        }

        registerVisit(visitorName, visitorPhoneNo, visitorEmail, purpose)
        return true
    }

    private fun registerVisit(visitorName: String, visitorPhoneNo: String, visitorEmail: String, purpose: String) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore

                createVisitor(visitorName, visitorPhoneNo, visitorEmail) { creation, visitorRef ->
                    if(creation){
                        Toast.makeText(this, "Visitor Created Successfully", Toast.LENGTH_SHORT).show()
                    }

                    getEmployee() {empDetails ->
                        if(empDetails.isEmpty()){
                            Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show()
                            return@getEmployee
                        }

                        val currentTime = Timestamp.now()

                        val visitMap = hashMapOf(
                            "visitor_ref" to visitorRef,
                            "employee_ref" to empDetails[0],
                            "checkInTime" to null,
                            "checkOutTime" to null,
                            "companyName" to empDetails[1],
                            "status" to "Pending",
                            "visitPurpose" to purpose,
                            "createdAt" to currentTime
                        )

                        Log.i("Map", visitMap.toString())

                        db.collection("visits")
                            .add(visitMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Visit Requested Successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, EmpHomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error registering visit", Toast.LENGTH_SHORT).show()
                                Log.w("Firestore", "Error adding document", e)
                            }
                    }
                }

            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createVisitor(visitorName: String, visitorPhoneNo: String, visitorEmail: String, callback: (Boolean, DocumentReference?) -> Unit) {
        val db = Firebase.firestore

        val userMap = hashMapOf(
            "username" to visitorName,
            "password" to visitorName.toLowerCase(),
            "usertype" to "Visitor"
        )

        val userDocRef = db.collection("user").document()
        Log.i("User", userDocRef.toString())

        userDocRef.set(userMap)
            .addOnSuccessListener {
                val visitorMap = hashMapOf(
                    "Email" to visitorEmail,
                    "PhoneNumber" to visitorPhoneNo,
                    "Image" to "",
                    "user_ref" to userDocRef
                )

                db.collection("visitors").document()
                    .set(visitorMap)
                    .addOnSuccessListener {
                        callback(true, userDocRef)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to register visitor: ${e.message}", Toast.LENGTH_LONG).show()
                        callback(false, null)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
                callback(false, null)
            }
    }

    private fun getEmployee(callback: (Array<Any?>) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val userRef = db.collection("user").document(userId)
                val empDetails: Array<Any?> = arrayOf(null, null)

                db.collection("employees")
                    .whereEqualTo("user_ref", userRef)
                    .get()
                    .addOnSuccessListener(){document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "Incorrect Employee Details", Toast.LENGTH_SHORT).show()
                            callback(empDetails)
                        } else {
                            for (d in document.documents){
                                empDetails[0] = d.reference
                                empDetails[1] = d.getString("Company")
                            }
                            callback(empDetails)
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this, "Error in Search", Toast.LENGTH_SHORT).show()
                        callback(empDetails)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        checkGalleryPermission()
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        checkCameraPermission()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY_REQUEST_CODE && data != null){
                val selectedImageUri = data.data
                uploadImageToFirebase(selectedImageUri)
                displayUploadedImage(selectedImageUri, "Selected Image from Gallery")
            }
            if(requestCode == CAMERA_REQUEST_CODE && data != null){
                val imageBitmap = data.extras?.get("data") as Bitmap
                val selectedImageUri = getImageUriFromBitmap(imageBitmap)
                uploadImageToFirebase(selectedImageUri)
                displayUploadedImage(selectedImageUri, "Clicked Image from Camera")
            }
        }
    }

    private fun displayUploadedImage(imageUri: Uri?, name: String) {
        selectImageLayout.visibility = View.GONE
        imageLayout.visibility = View.VISIBLE
        imageView.setImageURI(imageUri)
        imageName.text = name
    }

    private fun uploadImageToFirebase(uri: Uri?) {
        if(uri != null){
            val storageRef = FirebaseStorage.getInstance().reference

            if(checkSession()){
                val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
                val userId = sharedPreference.getString("userId", null)

                if(userId != null){
                    val imageRef = storageRef.child("images/${userId}.jpg")

                    val uploadImage = imageRef.putFile(uri)

                    uploadImage.addOnSuccessListener {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        imageFlag = true
                    }.addOnFailureListener {
                        Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
            }
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