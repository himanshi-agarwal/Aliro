package com.example.aliro

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

class SecurityHomeActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart

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
                R.id.home -> {
                    if (this !is SecurityHomeActivity) {
                        val intent = Intent(this, SecurityHomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Home", Toast.LENGTH_SHORT).show()
                    }
                }

                R.id.employee -> {
                    Toast.makeText(applicationContext, "Scan Employees's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.visitors -> {
                    Toast.makeText(applicationContext, "Scan Visitor's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Guest", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.parking -> {
                    Toast.makeText(applicationContext, "Vehicle Parking", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NumberPlateActivity::class.java)
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

        lineChart = findViewById(R.id.chart)
        pieChart = findViewById(R.id.pieChart)

        updateSidebarHeader()
        createLineChart()
        createPieChart()
    }

    private fun createLineChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.granularity = 1f
        lineChart.axisRight.isEnabled = false

        val legend: Legend = lineChart.legend
        legend.isEnabled = true

        val entries = mutableListOf<Entry>()
        entries.add(Entry(0f, 1f))
        entries.add(Entry(1f, 2f))
        entries.add(Entry(2f, 1.5f))
        entries.add(Entry(3f, 3f))
        entries.add(Entry(4f, 2.5f))

        val dataSet = LineDataSet(entries, "Default Values")
        dataSet.color = ContextCompat.getColor(this, R.color.primary)
        dataSet.valueTextColor = getColor(R.color.black)
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.other))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)

        val lineData = LineData(dataSets)

        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun createPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(R.color.white)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, "Category 1"))
        entries.add(PieEntry(30f, "Category 2"))
        entries.add(PieEntry(20f, "Category 3"))
        entries.add(PieEntry(10f, "Category 4"))

        val dataSet = PieDataSet(entries, "Default Values")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(
            ContextCompat.getColor(this, R.color.primary),
            ContextCompat.getColor(this, R.color.secondary),
            ContextCompat.getColor(this, R.color.other),
            ContextCompat.getColor(this, R.color.cancel)
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(R.color.black)

        pieChart.data = data
        pieChart.invalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun updateSidebarHeader() {
        val header = navView.getHeaderView(0)
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPreference.getString("userId", null)

        val headerUserName: TextView = header.findViewById(R.id.header_user_name)
        val headerUserType: TextView = header.findViewById(R.id.header_user_type)
        val headerUserProfile: ImageView = header.findViewById(R.id.header_user_profile_picture)

        headerUserName.text = sharedPreference.getString("userName", null)
        headerUserType.text = sharedPreference.getString("userType", null)

        if (checkSession() && userId != null) {
            Log.i("ID", userId)
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