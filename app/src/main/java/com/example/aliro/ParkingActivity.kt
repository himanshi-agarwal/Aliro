package com.example.aliro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.io.ByteArrayOutputStream

class ParkingActivity : AppCompatActivity() {
    private lateinit var spotButton: Button
    private lateinit var vehicleNumber: EditText
    private lateinit var vehicleModel: EditText
    private lateinit var duration: Spinner
    private lateinit var vehicleTypeGroup: RadioGroup
    private lateinit var visitorName: EditText
    private lateinit var visitorPhoneNumber: EditText
    private lateinit var parkingButton: Button
    private val CAMERA_PERMISSION_CODE = 101
    private val CAMERA_REQUEST_CODE = 103
    private var numberPlate: String = ""
    private var vehicleType: String = ""
    private var selectedDuration: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkVehicle { vehicle ->
            if(vehicle){
                setContentView(R.layout.parking)
            } else {
                setContentView(R.layout.noparking)

                spotButton = findViewById(R.id.spot)

                spotButton.setOnClickListener {
                    openCamera()
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Number Plate", null)
        return Uri.parse(path)
    }

    private fun openCamera() {
        checkCameraPermission()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_REQUEST_CODE && data != null){
            val imageBitmap = data.extras?.get("data") as Bitmap
            val imageUri = getImageUriFromBitmap(imageBitmap)
            numberPlate = readNumberPlate(imageUri)
            setUpForm()
        }
    }

    private fun readNumberPlate(uri: Uri?): String {
        return "RJ14 DA E 1234"
    }

    private fun setUpForm() {
        setContentView(R.layout.parking_register)

        vehicleNumber = findViewById(R.id.numberPlate)
        vehicleModel = findViewById(R.id.model)
        duration = findViewById(R.id.durationSpinner)
        vehicleTypeGroup = findViewById(R.id.vehicleTypeRadioGroup)
        visitorName = findViewById(R.id.VisitorName)
        visitorPhoneNumber = findViewById(R.id.visitorPhoneNumber)
        parkingButton = findViewById(R.id.parkingButton)

        val durationOptions = arrayOf("30 minutes", "1 hour", "2 hours", "4 hours", "8 hours", "All day")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durationOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        duration.adapter = adapter
        duration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDuration = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) { }
        }

        vehicleTypeGroup.setOnCheckedChangeListener{_, checkedIn ->
            when(checkedIn) {
                R.id.twoWheelerRadioButton -> {
                    vehicleType = "Two Wheeler"
                }
                R.id.fourWheelerRadioButton -> {
                    vehicleType = "Four Wheeler"
                }
            }
        }

        vehicleNumber.setText(numberPlate)

        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore

                val userRef = db.collection("user").document(userId)

                db.collection("visitors")
                    .whereEqualTo("user_ref", userRef)
                    .get()
                    .addOnSuccessListener() {document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "Visitor not Found", Toast.LENGTH_SHORT).show()
                        } else {
                            for (d in document.documents){
                                visitorPhoneNumber.setText(d.getLong("Phone Number").toString())
                            }
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this, "Visitor Details Failed", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }

        parkingButton.setOnClickListener(){
            submitParkingForm()
        }
    }

    private fun submitParkingForm() {
        val vehicleNumber = vehicleNumber.text.toString()
        val vehicleModel = vehicleModel.text.toString()
        val visitorName = visitorName.text.toString()
        val visitorPhoneNumber = visitorPhoneNumber.text.toString()

        if(vehicleNumber.isBlank() || vehicleModel.isBlank() || selectedDuration.isBlank() || vehicleType.isBlank()){
            Toast.makeText(this, "Vehicle Details Incomplete", Toast.LENGTH_SHORT).show()
            return
        }

        if(visitorName.isBlank() || visitorPhoneNumber.isBlank()){
            Toast.makeText(this, "Visitor Details Incomplete", Toast.LENGTH_SHORT).show()
            return
        }

        secureSpot(vehicleModel, visitorPhoneNumber)
    }

    private fun secureSpot(vehicleModel: String, visitorPhoneNumber: String) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore

                db.collection("parking")
                    .whereEqualTo("vehicleType", vehicleType)
                    .whereEqualTo("status", "Available")
                    .get()
                    .addOnSuccessListener(){document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "No Parking Space Available", Toast.LENGTH_SHORT).show()
                        } else {
                            for(d in document.documents){
                                val space = d.id
                                val spaceRef = db.collection("parking").document(space)
                                val userRef = db.collection("user").document(userId)
                                val currentTime = Timestamp.now()

                                val occupancyMap = hashMapOf(
                                    "space_Ref" to spaceRef,
                                    "vehicleNumber" to numberPlate,
                                    "Model" to vehicleModel,
                                    "Type" to vehicleType,
                                    "Duration" to selectedDuration,
                                    "user_Ref" to userRef,
                                    "Contact No" to visitorPhoneNumber.toLong(),
                                    "created_At" to currentTime
                                )

                                db.collection("occupancy")
                                    .add(occupancyMap)
                                    .addOnSuccessListener() {
                                        Toast.makeText(this, "Parking Successful", Toast.LENGTH_SHORT).show()
                                        db.collection("parking").document(space).update("status", "occupied")

                                        val intent = Intent(this, VisitorHomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener(){
                                        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                                        return@addOnFailureListener
                                    }

                                break
                            }
                        }
                    }
                    .addOnFailureListener(){
                        Toast.makeText(this,"No Parking Space Available", Toast.LENGTH_SHORT).show()
                        return@addOnFailureListener
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkVehicle (callback: (Boolean) -> Unit) {
        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore

                val userRef = db.collection("user").document(userId)

                db.collection("occupancy")
                    .whereEqualTo("user_Ref", userRef)
                    .get()
                    .addOnSuccessListener(){ document ->
                        if(document.isEmpty){
                            callback(false)
                        }
                        else{
                            callback(true)
                        }
                    }
                    .addOnFailureListener() {
                        Toast.makeText(this, "Error Fetching Records", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }
}
