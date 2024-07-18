package com.example.aliro

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        val drawerLayout : DrawerLayout = findViewById(R.id.navigation)
        val nav_view : NavigationView = findViewById(R.id.navbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nav_view.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.profile -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                R.id.nav_timings -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                R.id.nav_parking -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                R.id.nav_dairy -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                R.id.nav_register -> Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}