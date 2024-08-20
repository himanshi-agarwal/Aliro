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

class VisitorHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var user: ArrayList<String>
    private lateinit var username: TextView
    private lateinit var useremail: TextView
    private lateinit var userphone: TextView

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

        user = intent.getStringArrayListExtra("user") ?: arrayListOf()

        username = findViewById(R.id.visitor_username)
        useremail = findViewById(R.id.visitor_email)
        userphone = findViewById(R.id.visitor_phone)

        username.text = user[0]
        useremail.text = user[1]
        userphone.text = user[2]

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
                R.id.profile -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()

                R.id.pre_register -> Toast.makeText(applicationContext, "Pre-Register", Toast.LENGTH_SHORT).show()

                R.id.dairy -> Toast.makeText(applicationContext, "Dairy", Toast.LENGTH_SHORT).show()

                R.id.parking -> Toast.makeText(applicationContext, "Parking", Toast.LENGTH_SHORT).show()

                R.id.timings -> Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()

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