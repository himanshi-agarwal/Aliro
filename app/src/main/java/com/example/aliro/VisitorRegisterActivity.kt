package com.example.aliro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.navigation.NavigationView
import java.util.Calendar

class VisitorRegisterActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar
    private lateinit var visitDate : Button

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
        setContentView(R.layout.vis_register)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        visitDate = findViewById(R.id.visitDate)

        visitDate.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { view, year, monthOfYear, dayOfMonth ->
                    Log.i("Date", dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                }, year, month, day
            )
            datePickerDialog.show()
        }
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