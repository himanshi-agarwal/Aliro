package com.example.aliro

import android.content.Intent
import android.os.Bundle
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

class EmpHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var employee: ArrayList<String>
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

        employee = intent.getStringArrayListExtra("employee") ?: arrayListOf()

        empName = findViewById(R.id.emp_name)
        empEmail = findViewById(R.id.emp_email)
        empPhone = findViewById(R.id.emp_phone)
        empRole = findViewById(R.id.emp_role)
        empCompany = findViewById(R.id.emp_company)
        empLocation = findViewById(R.id.emp_location)

        empName.text = employee[0]
        empEmail.text = employee[1]
        empPhone.text = employee[2]

        toolbar = findViewById<Toolbar>(R.id.toolbar)
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
                    intent.putStringArrayListExtra("employee", employee)
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

                R.id.about -> Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()

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
}