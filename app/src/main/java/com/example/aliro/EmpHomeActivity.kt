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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore

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

        getUserData { employee ->
            if(checkSession() && employee != null){
                empName.text = employee[1]
                empid.text = employee[3]
                empEmail.text = employee[4]
                empPhone.text = employee[5]
                empRole.text = employee[6]
                empCompany.text = employee[7]

                updateSidebarHeader()
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
                    val intent = Intent(this, LogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile -> {
                    Toast.makeText(applicationContext, "Edit Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpEditActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Visitor", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
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
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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
                            employeeArray[3] = d.getString("EmpID")
                            employeeArray[4] = d.getString("Email")
                            employeeArray[5] = d.getString("Phone_Number")
                            employeeArray[6] =  d.getString("Company")
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

        val headerUserName : TextView = header.findViewById(R.id.header_user_name)
        val headerUserType : TextView = header.findViewById(R.id.header_user_type)

        headerUserName.text = sharedPreference.getString("userName", null)
        headerUserType.text = sharedPreference.getString("userType", null)
    }
}