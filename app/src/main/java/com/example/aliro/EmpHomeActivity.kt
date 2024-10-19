package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.bumptech.glide.Glide
import com.google.firebase.storage.storage

class EmpHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var empid : TextView
    private lateinit var empName: TextView
    private lateinit var empEmail: TextView
    private lateinit var empPhone: TextView
    private lateinit var empRole: TextView
    private lateinit var empCompany: TextView
    private lateinit var userProfile: ImageView

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

        empid = findViewById(R.id.emp_id)
        empName = findViewById(R.id.emp_name)
        empEmail = findViewById(R.id.emp_email)
        empPhone = findViewById(R.id.emp_phone)
        empRole = findViewById(R.id.emp_role)
        empCompany = findViewById(R.id.emp_company)
        userProfile = findViewById(R.id.user_profile)

        getUserData { employee ->
            if(checkSession() && employee != null){
                empName.text = employee[1]
                empid.text = employee[3]
                empEmail.text = employee[4]
                empPhone.text = employee[5]
                empRole.text = employee[6]
                empCompany.text = employee[7]

                updateSidebarHeader()
                updateUserProfile()
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.home -> {
                    if (this !is EmpHomeActivity) {
                        val intent = Intent(this, EmpHomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Home", Toast.LENGTH_SHORT).show()
                    }
                }

                R.id.logs -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpLogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile -> {
                    Toast.makeText(applicationContext, "Edit Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpEditActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Visitor", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpRegisterActivity::class.java)
                    startActivity(intent)
                }

                R.id.notification -> {
                    Toast.makeText(applicationContext, "Notifications", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpNotificationActivity::class.java)
                    startActivity(intent)
                }

                R.id.about -> {
                    Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpAboutActivity::class.java)
                    startActivity(intent)
                }

                R.id.logout -> {
                    logout()
                    Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
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

    private fun getUserData(callback: (Array<String?>?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)
            val userType = sharedPreference.getString("userType", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            val employeeArray : Array<String?> = arrayOf(userId, userName, userType, null, null, null, null, null)

            db.collection("employees")
                .whereEqualTo("user_ref", userRef)
                .get()
                .addOnSuccessListener { document ->
                    if (document.isEmpty) {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        for (d in document.documents) {
                            employeeArray[3] = d.getLong("EmpID").toString()
                            employeeArray[4] = d.getString("Email")
                            employeeArray[5] = d.getString("Phone_Number")
                            employeeArray[6] = d.getString("Company")
                            employeeArray[7] = d.getString("Role")
                        }
                        callback(employeeArray)
                    }
                }
                .addOnFailureListener {e ->
                    Toast.makeText(this, "Error Fetching Data", Toast.LENGTH_SHORT).show()
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
}