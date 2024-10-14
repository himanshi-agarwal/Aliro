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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SecurityVisitorRegistrationActivity: AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var phone_no: EditText
    private lateinit var email: EditText
    private lateinit var empID: EditText
    private lateinit var empName: EditText
    private lateinit var empCompany: EditText
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
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private val CAMERA_PERMISSION_CODE = 101
    private val STORAGE_PERMISSION_CODE = 102
    private val CAMERA_REQUEST_CODE = 103
    private val GALLERY_REQUEST_CODE = 104
    private var visitorUserID: String? = null
    private var selectedImageUri: Uri? = null
    private var imageFlag = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.security_visitor_registration)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                   val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                   Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                }

                R.id.employee -> {
                    Toast.makeText(applicationContext, "Scan Employee's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FaceRekognitionActivity::class.java)
                    startActivity(intent)
                }

                R.id.visitors -> {
                    Toast.makeText(applicationContext, "Scan Visitor's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Guest", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityVisitorRegistrationActivity::class.java)
                    startActivity(intent)
                }

                R.id.parking -> {
                    Toast.makeText(applicationContext, "Vehicle Parking", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NumberPlateActivity::class.java)
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

        name = findViewById(R.id.fullname)
        phone_no = findViewById(R.id.phone_no)
        email = findViewById(R.id.email)
        empID = findViewById(R.id.empid)
        empName = findViewById(R.id.empname)
        empCompany = findViewById(R.id.empcompany)
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

        updateSidebarHeader()

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

    private fun checkDetails(): Boolean{
        val visitorName = name.text.toString()
        val visitorPhoneNo = phone_no.text.toString()
        val visitorEmail = email.text.toString()
        val empId = empID.text.toString()
        val empName = empName.text.toString()
        val empCompany = empCompany.text.toString()
        val purpose = purpose.text.toString()
        val visitDate = visitDate.text.toString()

        if(visitorName.isBlank() || visitorPhoneNo.isBlank() || visitorEmail.isBlank()){
            Toast.makeText(this, "Visitor Details Incomplete", Toast.LENGTH_SHORT).show()
            return false
        }

        if(empId.isBlank() || empName.isBlank() || empCompany.isBlank()){
            Toast.makeText(this, "Employee Details Incomplete", Toast.LENGTH_SHORT).show()
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

        registerVisit(empId, empCompany, purpose)
        return true
    }

    private fun registerVisit(empId: String, empCompany: String, purpose: String) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore
                val name = name.text.toString()
                val phoneNumber = phone_no.text.toString()
                val email = email.text.toString()
                val visitDate = visitDate.text.toString()
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val parsedDate: Date? = dateFormat.parse(visitDate)
                val visitDateTimestamp: Timestamp? = parsedDate?.let { Timestamp(it) }

                createVisitor(name, phoneNumber, email) { visitorCreated, visitorRef ->
                    if(!visitorCreated){
                        Toast.makeText(this, "Error in creating Visitor Credentials", Toast.LENGTH_SHORT).show()
                    }

                    getEmployee(empId) {employeeRef ->
                        if(employeeRef == null){
                            Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show()
                        }

                        val currentTime = Timestamp.now()

                        val visitMap = hashMapOf(
                            "visitor_ref" to db.document("visitors/${visitorRef}"),
                            "employee_ref" to db.document("employees/${employeeRef}"),
                            "checkInTime" to null,
                            "checkOutTime" to null,
                            "companyName" to empCompany,
                            "status" to "pending",
                            "visitPurpose" to purpose,
                            "visitDate" to visitDateTimestamp,
                            "createdAt" to currentTime
                        )

                        db.collection("visits")
                            .add(visitMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Visit Requested Successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, SecurityHomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error registering visit", Toast.LENGTH_SHORT).show()
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

    private fun createVisitor(visitorName: String, visitorPhoneNo: String, visitorEmail: String, callback: (Boolean, String?) -> Unit) {
        val db = Firebase.firestore

        val userMap = hashMapOf(
            "username" to visitorName,
            "password" to visitorName.toLowerCase(),
            "usertype" to "Visitor"
        )

        val userDocRef = db.collection("user").document()
        visitorUserID = userDocRef.id

        userDocRef.set(userMap)
            .addOnSuccessListener {
                val visitorMap = hashMapOf(
                    "Email" to visitorEmail,
                    "PhoneNumber" to visitorPhoneNo,
                    "Image" to "",
                    "user_ref" to userDocRef
                )

                val visitorDocRef = db.collection("visitors").document()

                visitorDocRef.set(visitorMap)
                    .addOnSuccessListener {
                        uploadImageToFirebase(selectedImageUri)
                        callback(true, visitorDocRef.id)
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

    private fun getEmployee(empId: String, callback: (String?) -> Unit) {
        val db = Firebase.firestore

        db.collection("employees")
            .whereEqualTo("EmpID", empId.toInt())
            .get()
            .addOnSuccessListener(){document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Incorrect Employee Details", Toast.LENGTH_SHORT).show()
                    callback(null)
                } else {
                    val empRef = document.documents.firstOrNull()?.id
                    callback(empRef)
                }
            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error in Search", Toast.LENGTH_SHORT).show()
                callback(null)
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
                selectedImageUri = data.data
                displayUploadedImage(selectedImageUri, "Selected Image from Gallery")
            }
            if(requestCode == CAMERA_REQUEST_CODE && data != null){
                val imageBitmap = data.extras?.get("data") as Bitmap
                selectedImageUri = getImageUriFromBitmap(imageBitmap)
                displayUploadedImage(selectedImageUri, "Clicked Image from Camera")
            }
        }
        imageFlag = true
    }

    private fun uploadImageToFirebase(uri: Uri?) {
        if(uri != null){
            val storageRef = FirebaseStorage.getInstance().reference

            if(checkSession()){
                val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
                val userId = sharedPreference.getString("userId", null)

                if(userId != null){
                    val imageRef = storageRef.child("images/visitor/${visitorUserID}.jpg")
                    val uploadImage = imageRef.putFile(uri)

                    uploadImage.addOnSuccessListener {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
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

    private fun displayUploadedImage(imageUri: Uri?, name: String) {
        selectImageLayout.visibility = View.GONE
        imageLayout.visibility = View.VISIBLE
        imageView.setImageURI(imageUri)
        imageName.text = name
    }

    private fun checkSession(): Boolean {
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

    private fun updateSidebarHeader() {
        val header = navView.getHeaderView(0)
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPreference.getString("userId", null)

        val headerUserName: TextView = header.findViewById(R.id.header_user_name)
        val headerUserType: TextView = header.findViewById(R.id.header_user_type)
        val headerUserProfile: ImageView = header.findViewById(R.id.header_user_profile_picture)

        headerUserName.text = sharedPreference.getString("userName", null)
        headerUserType.text = sharedPreference.getString("userType", null)

        if (checkSession() && userId != null) {
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}