package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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

class EmpEditActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var pass : EditText
    private lateinit var newpass : EditText
    private lateinit var confirmpass : EditText
    private lateinit var saveButton : Button
    private lateinit var cancelButton : Button
    private var db = Firebase.firestore

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
        setContentView(R.layout.emp_edit_profile)
        enableEdgeToEdge()

        pass = findViewById(R.id.curr_pass)
        newpass = findViewById(R.id.new_pass)
        confirmpass = findViewById(R.id.confirm_pass)
        saveButton = findViewById(R.id.save)
        cancelButton = findViewById(R.id.cancel)

        saveButton.setOnClickListener(){
            val password = pass.text.toString()
            val newpassword = newpass.text.toString()
            val confirmpassword = confirmpass.text.toString()

            if(password.isBlank()){
                pass.error = "Filed is Empty"
            }

            if(newpassword.isBlank()){
                newpass.error = "Filed is Empty"
            }

            if(confirmpassword.isBlank()){
                confirmpass.error = "Filed is Empty"
            }

            editProfile(password, newpassword, confirmpassword)
        }

        cancelButton.setOnClickListener(){
            cancelUpdate(pass, newpass, confirmpass)
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open , R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateSidebarHeader()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
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
                    Toast.makeText(applicationContext, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
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

    private fun checkPassword(p: String, cp: String) : Boolean{
        return p == cp
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun editProfile(password : String, newpassword : String, confirmpassword : String){
        if (checkPassword(newpassword, confirmpassword) && checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)

            val userRef = db.collection("user").document(userId!!)

            db.collection("user")
                .whereEqualTo(FieldPath.documentId(), userId)
                .get()
                .addOnSuccessListener { document ->
                    for (d in document.documents) {
                        val dbPassword = d.getString("password")
                        if (dbPassword != password) {
                            Toast.makeText(this, "Incorrect Current Password!", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                    }

                    userRef.update(
                        mapOf(
                            "username" to userName,
                            "password" to newpassword,
                            "usertype" to "Employee"
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EmpHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error Updating Password", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "User Details Not found", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cancelUpdate(pass : EditText, newpass : EditText, confirmpass : EditText){
        pass.text.clear()
        newpass.text.clear()
        confirmpass.text.clear()

        Toast.makeText(this, "Changes Discarded", Toast.LENGTH_SHORT).show()
        finish()
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