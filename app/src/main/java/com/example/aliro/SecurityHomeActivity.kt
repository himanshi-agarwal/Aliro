package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

//import com.jjoe64.graphview.GraphView
//import com.jjoe64.graphview.series.DataPoint
//import com.jjoe64.graphview.series.LineGraphSeries


class SecurityHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar

//    lateinit var lineGraphView: GraphView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.security_home)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    navView.setNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.emp -> {
                if (this !is SecurityHomeActivity) {
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "Scan Employee's Face", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.vis -> {
                Toast.makeText(applicationContext, "Scan Visitor's Face", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LogsActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.pre_register -> {
                Toast.makeText(applicationContext, "Register Guest", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, EmpEditActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.park -> {
                Toast.makeText(applicationContext, "Vehicle Parking", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ParkingActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.logout -> {
                Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        true
    }


//        lineGraphView = findViewById(R.id.idGraphView)
//
//        // on below line we are adding data to our graph view.
//        val series: LineGraphSeries<DataPoint> = LineGraphSeries(
//            arrayOf(
//                // on below line we are adding
//                // each point on our x and y axis.
//                DataPoint(0.0, 1.0),
//                DataPoint(1.0, 3.0),
//                DataPoint(2.0, 4.0),
//                DataPoint(3.0, 9.0),
//                DataPoint(4.0, 6.0),
//                DataPoint(5.0, 3.0),
//                DataPoint(6.0, 6.0),
//                DataPoint(7.0, 1.0),
//                DataPoint(8.0, 2.0)
//            )
//        )
//
//        // on below line adding animation
//        lineGraphView.animate()
//
//        // on below line we are setting scrollable
//        // for point graph view
//        lineGraphView.viewport.isScrollable = true
//
//        // on below line we are setting scalable.
//        lineGraphView.viewport.isScalable = true
//
//        // on below line we are setting scalable y
//        lineGraphView.viewport.setScalableY(true)
//
//        // on below line we are setting scrollable y
//        lineGraphView.viewport.setScrollableY(true)
//
//        // on below line we are setting color for series.
//        series.color = R.color.black
//
//        // on below line we are adding
//        // data series to our graph view.
//        lineGraphView.addSeries(series)
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
}