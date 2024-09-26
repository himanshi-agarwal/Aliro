package com.example.aliro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.io.ByteArrayOutputStream

data class ParkingRecord (
    val type: String = "",
    val vehicleNumber: String = "",
    val space: String = "",
    val model: String = ""
)

class ParkingActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
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
            if(vehicle != null){
                setContentView(R.layout.parking)

                toolbar = findViewById(R.id.toolbar)
                setSupportActionBar(toolbar)

                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)

                showParkingRecords(vehicle)
            } else {
                setContentView(R.layout.noparking)

                toolbar = findViewById(R.id.toolbar)
                setSupportActionBar(toolbar)

                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)

                spotButton = findViewById(R.id.spot)

                spotButton.setOnClickListener {
                    openCamera()
                }
            }

            toolbar.setNavigationOnClickListener {
                val intent = Intent(this, VisitorHomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

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

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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

    private fun checkVehicle (callback: (QuerySnapshot?) -> Unit) {
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
                            callback(null)
                        }
                        else{
                            Log.i("Data", document.documents.toString())
                            callback(document)
                        }
                    }
                    .addOnFailureListener() {
                        Toast.makeText(this, "Error Fetching Records", Toast.LENGTH_SHORT).show()
                        callback(null)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun showParkingRecords(records: QuerySnapshot) {
        val parkingLayout = findViewById<LinearLayout>(R.id.parkingLayout)
        parkingLayout.removeAllViews()

        val recordPadding = resources.getDimensionPixelSize(R.dimen.record_padding)
        val imageDimension = resources.getDimensionPixelSize(R.dimen.image_dimen)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin)
        val spaceWidth = resources.getDimensionPixelSize(R.dimen.space_width)
        val spacePadding = resources.getDimensionPixelSize(R.dimen.space_padding)

        for (r in records){
            val record = r.toObject(ParkingRecord::class.java)
            val recordLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(recordPadding, recordPadding, recordPadding, recordPadding)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                orientation = LinearLayout.HORIZONTAL
            }

            val vehicleImage = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    imageDimension,
                    imageDimension
                ).apply {
                    marginEnd = imageMarginEnd
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            if(r.getString("Type") == "Four Wheeler"){
                vehicleImage.setImageResource(R.drawable.car)
            } else {
                vehicleImage.setImageResource(R.drawable.bike)
            }

            val innerLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = LinearLayout.VERTICAL
            }

            val vehicleNumber = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = r.getString("vehicleNumber")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(ResourcesCompat.getFont(this@ParkingActivity, R.font.exo_2_semibold), Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val duration = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = r.getString("Duration")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(ResourcesCompat.getFont(this@ParkingActivity, R.font.lato_italic), Typeface.ITALIC)
                gravity = Gravity.CENTER
            }

            innerLayout.addView(vehicleNumber)
            innerLayout.addView(duration)

            val locationText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    spaceWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setPadding(spacePadding, spacePadding, spacePadding, spacePadding)
                }
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            r.getDocumentReference("space_Ref")?.let {spaceRef ->
                getLocation(spaceRef) { location ->
                    if (location != null) {
                        locationText.text = location
                    } else {
                        locationText.text = "Location not found"
                    }
                }
            }

            recordLayout.addView(vehicleImage)
            recordLayout.addView(innerLayout)
            recordLayout.addView(locationText)

            parkingLayout.addView(recordLayout)
        }
    }

    private fun getLocation(space: DocumentReference, callback: (String?) -> Unit) {
        space.get()
        .addOnSuccessListener(){document ->
            if (document.exists()) {
                val location = document.getString("space")
                callback(location)
            } else {
                callback(null)
            }
        }
        .addOnFailureListener(){
            Toast.makeText(this, "Error Fetching Location", Toast.LENGTH_SHORT).show()
            callback(null)
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