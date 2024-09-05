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

class EmpHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var empName: TextView
    private lateinit var empEmail: TextView
    private lateinit var empPhone: TextView
    private lateinit var empRole: TextView
    private lateinit var empCompany: TextView
    private lateinit var empLocation: TextView

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
        setContentView(R.layout.emp_home)

        empName = findViewById(R.id.emp_name)
        empEmail = findViewById(R.id.emp_email)
        empPhone = findViewById(R.id.emp_phone)
        empRole = findViewById(R.id.emp_role)
        empCompany = findViewById(R.id.emp_company)
        empLocation = findViewById(R.id.emp_location)

        getUserData { employee ->
            if(checkSession() && employee != null){
                empName.text = employee[1]
                empEmail.text = employee[1]
                empPhone.text = employee[1]
                empRole.text = employee[3]
                empCompany.text = employee[4]
                empLocation.text = employee[5]
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
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserData(callback: (Array<String?>?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)
            val userType = sharedPreference.getString("userType", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            db.collection("employee")
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

                        val employeeArray = arrayOf(userId, userName, userType, userRole, userCompany, userLocation)
                        callback(employeeArray)
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
}