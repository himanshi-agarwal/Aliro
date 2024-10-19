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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.Calendar
import java.util.Date

class SecurityHomeActivity : AppCompatActivity() {
    private lateinit var visitorsCount: TextView
    private lateinit var parkingSlotCount: TextView
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
                    val intent = Intent(this, FaceRekognitionActivity::class.java)
                    startActivity(intent)
                }

                R.id.visitors -> {
                    Toast.makeText(applicationContext, "Scan Visitor's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityVisitorsApprovalActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Guest", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityVisitorRegistrationActivity::class.java)
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

        visitorsCount = findViewById(R.id.visitors)
        parkingSlotCount = findViewById(R.id.parkingSlot)
        lineChart = findViewById(R.id.chart)
        pieChart = findViewById(R.id.pieChart)

        getVisitorsCount { count ->
            visitorsCount.text = count.toString()
        }

        getParkingSlots { count ->
            parkingSlotCount.text = count.toString()
        }

        getVisitorCountsByWeek { visitorCounts ->
            createLineChart(visitorCounts)
        }

        updateSidebarHeader()
        createPieChart()
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

    private fun getVisitorsCount(callback: (Int) -> Unit) {
        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                val endOfDay = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                db.collection("visits")
                    .whereGreaterThanOrEqualTo("checkInTime", startOfDay)
                    .whereLessThan("checkInTime", endOfDay)
                    .get()
                    .addOnSuccessListener() {document ->
                        if (document.isEmpty){
                            callback(0)
                        } else {
                            callback(document.documents.size)
                        }
                    }
                    .addOnFailureListener() {
                        Toast.makeText(this, "Failed to Fetch Count", Toast.LENGTH_SHORT).show()
                        callback(0)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
                callback(0)
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
            callback(0)
        }
    }

    private fun getParkingSlots(callback: (Int) -> Unit) {
        if(checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null) {
                val db = Firebase.firestore

                db.collection("parking")
                    .whereEqualTo("status", "Available")
                    .get()
                    .addOnSuccessListener() {document ->
                        if (document.isEmpty){
                            callback(0)
                        } else {
                            callback(document.documents.size)
                        }
                    }
                    .addOnFailureListener() {
                        Toast.makeText(this, "Failed to Fetch Count", Toast.LENGTH_SHORT).show()
                        callback(0)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
                callback(0)
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
            callback(0)
        }
    }

    private fun createLineChart(visitorCountsByWeek: List<Int>) {
        lineChart.description.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelCount = 4
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Week 1", "Week 2", "Week 3", "Week 4"))

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.granularity = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = visitorCountsByWeek.size.toFloat()
        lineChart.axisRight.isEnabled = false

        val legend: Legend = lineChart.legend
        legend.isEnabled = true

        val entries = mutableListOf<Entry>()
        for (i in visitorCountsByWeek.indices) {
            entries.add(Entry(i.toFloat(), visitorCountsByWeek[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "Visitors per Week")
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

    private fun getVisitorCountsByWeek(callback: (List<Int>) -> Unit) {
        val db = Firebase.firestore

        val startOfMonth = getStartOfMonth()
        val endOfMonth = getEndOfMonth()

        db.collection("visits")
            .whereGreaterThanOrEqualTo("checkInTime", startOfMonth)
            .whereLessThanOrEqualTo("checkInTime", endOfMonth)
            .get()
            .addOnSuccessListener { documents ->
                val visitorCounts = IntArray(4) { 0 }

                for (document in documents) {
                    val checkInTime = document.getTimestamp("checkInTime")?.toDate()
                    if (checkInTime != null) {
                        val weekNumber = getWeekOfMonth(checkInTime)
                        visitorCounts[weekNumber - 1]++
                    }
                }

                callback(visitorCounts.toList())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
                callback(listOf(0, 0, 0, 0))
            }
    }

    private fun getStartOfMonth(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    private fun getEndOfMonth(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.time
    }

    private fun getWeekOfMonth(date: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal.get(Calendar.WEEK_OF_MONTH)
    }

    private fun createPieChart() {
        val db = Firebase.firestore

        db.collection("parking")
            .whereEqualTo("status", "Occupied")
            .get()
            .addOnSuccessListener { documents ->
                var twoWheelerCount = 0
                var fourWheelerCount = 0

                for (document in documents) {
                    val vehicleType = document.getString("vehicleType")
                    if (vehicleType != null) {
                        if (vehicleType == "Two Wheeler") {
                            twoWheelerCount++
                        } else if (vehicleType == "Four Wheeler") {
                            fourWheelerCount++
                        }
                    }
                }

                Log.i("Two", twoWheelerCount.toString())
                Log.i("Four", fourWheelerCount.toString())

                val totalCount = twoWheelerCount + fourWheelerCount
                setupPieChart(twoWheelerCount, fourWheelerCount, totalCount)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch parking data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupPieChart(twoWheelerCount: Int, fourWheelerCount: Int, totalCount: Int) {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.white))
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        val entries = ArrayList<PieEntry>()
        if (totalCount > 0) {
            val twoWheelerPercentage = (twoWheelerCount.toFloat() / totalCount) * 100
            val fourWheelerPercentage = (fourWheelerCount.toFloat() / totalCount) * 100

            entries.add(PieEntry(twoWheelerPercentage, "Two-Wheeler"))
            entries.add(PieEntry(fourWheelerPercentage, "Four-Wheeler"))
        }

        val dataSet = PieDataSet(entries, "Parking Area")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(
            ContextCompat.getColor(this, R.color.primary),
            ContextCompat.getColor(this, R.color.secondary)
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.black))

        pieChart.data = data
        pieChart.invalidate()
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