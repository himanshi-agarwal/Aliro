package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class VisitorHomeActivity : AppCompatActivity() {
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var visitorName: TextView
    private lateinit var register : Button
    private lateinit var parking : Button
    private lateinit var logs : Button
    private lateinit var diary : Button

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.about -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AboutActivity::class.java)
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
        setContentView(R.layout.visitor_home)

        visitorName = findViewById(R.id.visitor_username)
        register = findViewById(R.id.pre_register)
        logs = findViewById(R.id.logs)
        parking = findViewById(R.id.parking)
        diary = findViewById(R.id.diary)

        getUserData { visitor ->
            if(checkSession() && visitor != null){
                visitorName.text = visitor[1]
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        register.setOnClickListener(){
            val intent = Intent(this, VisitorRegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        logs.setOnClickListener(){
            val intent = Intent(this, LogsActivity::class.java)
            startActivity(intent)
            finish()
        }

        parking.setOnClickListener(){
            val intent = Intent(this, ParkingActivity::class.java)
            startActivity(intent)
            finish()
        }

        diary.setOnClickListener(){
            val intent = Intent(this, VisitorDiaryActivity::class.java)
            startActivity(intent)
            finish()
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

    private fun updateSidebarHeader() {
        val header = navView.getHeaderView(0)
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)

        val headerUserName : TextView = header.findViewById(R.id.header_user_name)
        val headerUserType : TextView = header.findViewById(R.id.header_user_type)

        headerUserName.text = sharedPreference.getString("userName", null)
        headerUserType.text = sharedPreference.getString("userType", null)
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
}