package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class VisitorHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var visitorName: TextView
    private lateinit var visitorEmail: TextView
    private lateinit var visitorPhone: TextView

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
        enableEdgeToEdge()
        setContentView(R.layout.visitor_home)

        visitorName = findViewById(R.id.visitor_username)
        visitorEmail = findViewById(R.id.visitor_email)
        visitorPhone = findViewById(R.id.visitor_phone)

        getUserData { visitor ->
            if(checkSession() && visitor != null){
                visitorName.text = visitor[1]
                visitorEmail.text = visitor[1]
                visitorPhone.text = visitor[1]
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
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
                R.id.profile -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()

                R.id.pre_register -> Toast.makeText(applicationContext, "Pre-Register", Toast.LENGTH_SHORT).show()

                R.id.dairy -> Toast.makeText(applicationContext, "Dairy", Toast.LENGTH_SHORT).show()

                R.id.parking -> Toast.makeText(applicationContext, "Parking", Toast.LENGTH_SHORT).show()

                R.id.timings -> Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()

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

    private fun getUserData(callback: (Array<String?>?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)
            val userType = sharedPreference.getString("userType", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            db.collection("visitor")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener { document ->
                    if (document.isEmpty) {
                        Log.d("Id", userId.toString())
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        var userCompany : String? = null
                        var userRole : String? = null
                        var userLocation : String? = null

                        for (d in document.documents) {
                            userCompany = d.getString("Company").toString()
                            userRole = d.getString("Role").toString()
                            userLocation = d.getString("location").toString()
                        }

                        val visitorArray = arrayOf(userId, userName, userType, userRole, userCompany, userLocation)
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

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}