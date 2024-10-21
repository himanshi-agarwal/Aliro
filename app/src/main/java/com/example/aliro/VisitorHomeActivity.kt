package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class VisitorHomeActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar
    private lateinit var userProfile: ImageView
    private lateinit var visitorName: TextView
    private lateinit var register : ImageButton
    private lateinit var parking : ImageButton
    private lateinit var logs : ImageButton
    private lateinit var diary : ImageButton
    private lateinit var notification : ImageButton

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "Already in Profile", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.about -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VisitorAboutActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.logout -> {
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
        setContentView(R.layout.visitor_home)

        visitorName = findViewById(R.id.visitor_username)
        userProfile = findViewById(R.id.user_profile)
        register = findViewById(R.id.pre_register)
        logs = findViewById(R.id.logs)
        parking = findViewById(R.id.parking)
        diary = findViewById(R.id.diary)
        notification = findViewById(R.id.notification)

        getUserData { visitor ->
            if(checkSession() && visitor != null){
                visitorName.text = visitor[1]
                updateUserProfile()
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        register.setOnClickListener(){
            val intent = Intent(this, VisitorRegisterActivity::class.java)
            startActivity(intent)
        }

        logs.setOnClickListener(){
            val intent = Intent(this, VisitorLogsActivity::class.java)
            startActivity(intent)
        }

        parking.setOnClickListener(){
            val intent = Intent(this, ParkingActivity::class.java)
            startActivity(intent)
        }

        diary.setOnClickListener(){
            val intent = Intent(this, VisitorDiaryActivity::class.java)
            startActivity(intent)
        }

        notification.setOnClickListener(){
            val intent = Intent(this, VisitorNotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getUserData(callback: (Array<String?>?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)
            val userType = sharedPreference.getString("userType", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            val visitorArray : Array<String?> = arrayOf(userId, userName, userType, null, null)

            db.collection("visitors")
                .whereEqualTo("user_ref", userRef)
                .get()
                .addOnSuccessListener { document ->
                    if (document.isEmpty) {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        for (d in document.documents) {
                            visitorArray[3] = d.getString("Email").toString()
                            visitorArray[4] = d.getLong("Phone Number").toString()
                        }
                        callback(visitorArray)
                    }
                }
                .addOnFailureListener {e ->
                    Log.e("Error", e.message.toString())
                    Toast.makeText(this, "Error Fetching Data", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore Error", "Error fetching employee document", e)
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
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
            Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}