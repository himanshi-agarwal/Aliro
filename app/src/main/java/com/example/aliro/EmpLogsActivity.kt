package com.example.aliro

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class LogEntry(
    val inTime: Timestamp? = null,
    val outTime: Timestamp? = null,
    val duration: String = "",
)

class EmpLogsActivity: AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar
    private lateinit var empName : TextView
    private lateinit var empID : TextView
    private lateinit var empRole : TextView
    private lateinit var empCompany : TextView

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
        setContentView(R.layout.emp_logs)

        empName = findViewById(R.id.emp_name)
        empID = findViewById(R.id.emp_id)
        empRole = findViewById(R.id.emp_role)
        empCompany = findViewById(R.id.emp_company)

        getUserData { employee ->
            if(checkSession() && employee != null){
                empName.text = employee[1]
                empID.text = employee[2]
                empRole.text = employee[3]
                empCompany.text = employee[4]

                updateSidebarHeader()
                fetchLogsByUser()
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
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.logs -> {
                    if (this !is EmpLogsActivity) {
                        val intent = Intent(this, EmpLogsActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Logs", Toast.LENGTH_SHORT).show()
                    }
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
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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

    private fun getUserData(callback: (Array<String?>?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)
            val userName = sharedPreference.getString("userName", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            val employeeArray : Array<String?> = arrayOf(userId, userName, null, null, null)

            db.collection("employees")
                .whereEqualTo("user_ref", userRef)
                .get()
                .addOnSuccessListener { document ->
                    if (document.isEmpty) {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        for (d in document.documents) {
                            employeeArray[2] = d.getLong("EmpID").toString()
                            employeeArray[3] =  d.getString("Role")
                            employeeArray[4] = d.getString("Company")
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

    private fun fetchLogsByUser() {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            val db = Firebase.firestore
            val userRef = db.collection("user").document(userId!!)

            db.collection("logs")
                .whereEqualTo("user_ref", userRef)
                .get()
                .addOnSuccessListener { documents ->
                    val logsByDate = mutableMapOf<String, MutableList<LogEntry>>()

                    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    dateFormatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")

                    for (document in documents) {
                        val log = document.toObject(LogEntry::class.java)
                        val logDateFormatted = log.inTime?.let { dateFormatter.format(it.toDate()) } ?: "Unknown Date"

                        val formattedLog = LogEntry(
                            inTime = log.inTime,
                            outTime = log.outTime,
                            duration = calculateDuration(log.inTime, log.outTime)
                        )

                        Log.i("LOG", formattedLog.toString())
                        logsByDate.getOrPut(logDateFormatted) { mutableListOf() }.add(formattedLog)
                    }

                    displayLogsByDate(logsByDate)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error in Logs", Toast.LENGTH_SHORT).show()
                    Log.w("Firestore", "Error getting documents: ", exception)
                }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayLogsByDate(logsByDate: Map<String, List<LogEntry>>) {
        val parentLayout = findViewById<LinearLayout>(R.id.tableparent)
        parentLayout.removeAllViews()

        val datePadding = resources.getDimensionPixelSize(R.dimen.date_padding)
        val tablePadding = resources.getDimensionPixelSize(R.dimen.table_padding)
        val tableMargin = resources.getDimensionPixelSize(R.dimen.table_margin)
        val rowPadding = resources.getDimensionPixelSize(R.dimen.table_row_padding)

        for ((date, logs) in logsByDate) {
            val dateTextView = TextView(this).apply {
                text = "Date: ${date}"
                setPadding(datePadding, datePadding, datePadding, datePadding)
                gravity = Gravity.START
                setTextColor(ContextCompat.getColor(context, R.color.black))
                typeface = ResourcesCompat.getFont(context, R.font.lato_italic)
                setTypeface(typeface, Typeface.BOLD)
            }
            parentLayout.addView(dateTextView)

            val tableLayout = TableLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(tableMargin, 0, tableMargin, 0)
                }
                setPadding(tablePadding, tablePadding, tablePadding, tablePadding)
                gravity = Gravity.START
                isStretchAllColumns = true
                isShrinkAllColumns = true
                background = ContextCompat.getDrawable(context, R.drawable.rounded_corner)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
            }

            val headerRow = TableRow(this).apply {
                addView(createHeaderTextView("In Time"))
                addView(createHeaderTextView("Out Time"))
                addView(createHeaderTextView("Duration"))
            }
            tableLayout.addView(headerRow)

            for (log in logs) {
                val tableRow = TableRow(this).apply {
                    setPadding(rowPadding, rowPadding, rowPadding, rowPadding)
                    setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                tableRow.addView(createTextView(formatTimestampToTimeString(log.inTime)))
                tableRow.addView(createTextView(formatTimestampToTimeString(log.outTime)))
                tableRow.addView(createTextView(log.duration))

                tableLayout.addView(tableRow)
            }

            parentLayout.addView(tableLayout)
        }
    }

    private fun createTextView(text: String): TextView {
        val textPadding = resources.getDimensionPixelSize(R.dimen.text_padding)
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(textPadding, textPadding, textPadding, textPadding)
        }
    }

    private fun createHeaderTextView(text: String): TextView {
        val textPadding = resources.getDimensionPixelSize(R.dimen.text_padding)
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(textPadding, textPadding, textPadding, textPadding)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setTypeface(typeface, Typeface.BOLD)
        }
    }

    private fun formatTimestampToTimeString(timestamp: Timestamp?): String {
        return timestamp?.let {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.format(it.toDate())
        } ?: "--:--:--"
    }

    private fun calculateDuration(inTime: Timestamp?, outTime: Timestamp?): String {
        if (inTime == null || outTime == null) {
            return "--:--:--"
        }

        val inDate = inTime.toDate()
        val outDate = outTime.toDate()

        val durationMillis = outDate.time - inDate.time

        val durationSeconds = durationMillis / 1000
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this@EmpLogsActivity)
        builder.setMessage("Do you want to logout?")

        builder.setTitle("ALERT!")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes") { _, _ ->
            val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}