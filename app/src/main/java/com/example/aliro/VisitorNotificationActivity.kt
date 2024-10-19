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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class VisitorNotificationActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.visitor_notification)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        getNotifications { notifications ->
            if(notifications != null){
                showNotifications(notifications)
            }
        }
    }

    private fun getNotifications (callback: (QuerySnapshot?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val userRef = db.collection("user").document(userId)

                db.collection("visitors")
                    .whereEqualTo("user_ref", userRef)
                    .get()
                    .addOnSuccessListener() {document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
                        } else {
                            val visitorRef = document.documents.firstOrNull()?.reference

                            db.collection("visits")
                                .whereEqualTo("visitor_ref", visitorRef)
                                .get()
                                .addOnSuccessListener() {document ->
                                    if(document.isEmpty){
                                        setContentView(R.layout.no_notifications)
                                        callback(null)
                                    } else {
                                        Log.i("Data", document.documents.toString())
                                        callback(document)
                                    }
                                }
                        }
                    }
                    .addOnFailureListener() {e ->
                        Toast.makeText(this, "Error Fetching Records", Toast.LENGTH_SHORT).show()
                        Log.e("Error", e.printStackTrace().toString())
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun showNotifications(notifications: QuerySnapshot) {
        val notificationLayout = findViewById<LinearLayout>(R.id.notificationParent)
        notificationLayout.removeAllViews()

        val notificationPadding = resources.getDimensionPixelSize(R.dimen.record_padding)
        val imageDimension = resources.getDimensionPixelSize(R.dimen.image_dimen)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin)
        val spacePadding = resources.getDimensionPixelSize(R.dimen.space_padding)

        for (n in notifications){
            val recordLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(notificationPadding, notificationPadding, notificationPadding, notificationPadding)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                orientation = LinearLayout.HORIZONTAL
            }

            val visitorImage = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    imageDimension,
                    imageDimension
                ).apply {
                    marginEnd = imageMarginEnd
                }
                setImageResource(R.drawable.account)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val innerLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = LinearLayout.VERTICAL
            }

            val companyName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = n.getString("companyName")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(ResourcesCompat.getFont(this@VisitorNotificationActivity, R.font.exo_2_semibold), Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val purpose = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = n.getString("visitPurpose")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(ResourcesCompat.getFont(this@VisitorNotificationActivity, R.font.lato_italic), Typeface.ITALIC)
                gravity = Gravity.CENTER
            }

            innerLayout.addView(companyName)
            innerLayout.addView(purpose)

            val statusImage = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(spacePadding, spacePadding, spacePadding, spacePadding)
                setImageResource(R.drawable.pending_approval)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            if(n.getString("status") != "Pending"){
                statusImage.setImageResource(R.drawable.approved)
            }

//            n.getDocumentReference("visitor_ref")?.let {userRef ->
//                getVisitorName(userRef){name ->
//                    if(name != null){
//                        visitorName.text = name
//                        getUserProfile(visitorImage, visitorName.text.toString())
//                    }
//                    else{
//                        visitorName.text = "User"
//                    }
//                }
//            }

            recordLayout.addView(visitorImage)
            recordLayout.addView(innerLayout)
            recordLayout.addView(statusImage)

            notificationLayout.addView(recordLayout)
        }
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
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