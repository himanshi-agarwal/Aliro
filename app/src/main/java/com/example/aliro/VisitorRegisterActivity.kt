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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.Calendar

class VisitorRegisterActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar
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
    private val CAMERA_PERMISSION_CODE = 101
    private val STORAGE_PERMISSION_CODE = 102
    private val CAMERA_REQUEST_CODE = 103
    private val GALLERY_REQUEST_CODE = 104
    var imageFlag = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VisitorHomeActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.about -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VisitorAboutActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.logout -> {
                Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.vis_menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.vis_register)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, VisitorHomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
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

    private fun checkSession(): Boolean{
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

                getVisitor(userId) { visitorRef ->
                    if(visitorRef == null){
                        Toast.makeText(this, "Visitor not found", Toast.LENGTH_SHORT).show()
                        return@getVisitor
                    }

                    getEmployee(empId) {employeeRef ->
                        if(employeeRef == null){
                            Toast.makeText(this, "Visitor not found", Toast.LENGTH_SHORT).show()
                            return@getEmployee
                        }

                        val currentTime = Timestamp.now()

                        val visitMap = hashMapOf(
                            "visitor_ref" to "visitors/${visitorRef}",
                            "employee_ref" to "employees/${employeeRef}",
                            "checkInTime" to null,
                            "checkOutTime" to null,
                            "companyName" to empCompany,
                            "status" to "pending",
                            "visitPurpose" to purpose,
                            "createdAt" to currentTime
                        )

                        db.collection("visits")
                            .add(visitMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Visit Requested Successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, VisitorHomeActivity::class.java)
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

    private fun getVisitor(userId : String, callback: (String?) -> Unit) {
        val db = Firebase.firestore

        val userRef = db.collection("user").document(userId)

        db.collection("visitors")
            .whereEqualTo("user_ref", userRef)
            .get()
            .addOnSuccessListener(){document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Incorrect Visitor Details", Toast.LENGTH_SHORT).show()
                    callback(null)
                } else {
                    val visitorRef = document.documents.firstOrNull()?.id
                    callback(visitorRef)
                }
            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error in Search", Toast.LENGTH_SHORT).show()
                callback(null)
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

    private fun displayUploadedImage(imageUri: Uri?, name: String) {
        selectImageLayout.visibility = View.GONE
        imageLayout.visibility = View.VISIBLE
        imageView.setImageURI(imageUri)
        imageName.text = name
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