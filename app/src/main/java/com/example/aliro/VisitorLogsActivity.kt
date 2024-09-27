package com.example.aliro

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class VisitorLog(
    val inTime: Timestamp? = null,
    val outTime: Timestamp? = null,
    val duration: String = "",
)

class VisitorLogsActivity: AppCompatActivity() {
    private lateinit var toolbar : Toolbar

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VisitorHomeActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.about -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, VisitorAboutActivity::class.java)
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
        setContentView(R.layout.visitor_logs)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, VisitorHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchLogsByUser()
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
                        val log = document.toObject(VisitorLog::class.java)
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
        } ?: "Unknown"
    }

    private fun calculateDuration(inTime: Timestamp?, outTime: Timestamp?): String {
        if (inTime == null || outTime == null) {
            return "Invalid times"
        }

        val inDate = inTime.toDate()
        val outDate = outTime.toDate()

        val durationMillis = outDate.time - inDate.time

        val durationSeconds = durationMillis / 1000
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this@VisitorLogsActivity)
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
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}