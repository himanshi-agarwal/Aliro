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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

data class NotificationRecord (
    val type: String = "",
    val vehicleNumber: String = "",
    val space: String = "",
    val model: String = ""
)

class EmpNotificationActivity : AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar

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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.emp_notifications)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navbar)

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateSidebarHeader()

        getNotifications { notifications ->
            if(notifications != null){
                showNotifications(notifications)
            } else {
                createNoNotification()
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.logs -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpLogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile -> {
                    Toast.makeText(applicationContext, "Edit Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpEditActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Visitor", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpRegisterActivity::class.java)
                    startActivity(intent)
                }

                R.id.notification -> {
                    if (this !is EmpNotificationActivity) {
                        val intent = Intent(this, EmpHomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Notifications", Toast.LENGTH_SHORT).show()
                    }
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

    private fun createNoNotification() {
        setContentView(R.layout.no_notifications)

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
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpHomeActivity::class.java)
                    startActivity(intent)
                }

                R.id.logs -> {
                    Toast.makeText(applicationContext, "Logs", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpLogsActivity::class.java)
                    startActivity(intent)
                }

                R.id.profile -> {
                    Toast.makeText(applicationContext, "Edit Profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpEditActivity::class.java)
                    startActivity(intent)
                }

                R.id.pre_register -> {
                    Toast.makeText(applicationContext, "Register Visitor", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, EmpRegisterActivity::class.java)
                    startActivity(intent)
                }

                R.id.notification -> {
                    if (this !is EmpNotificationActivity) {
                        val intent = Intent(this, EmpHomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Already in Notifications", Toast.LENGTH_SHORT).show()
                    }
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

        updateSidebarHeader()
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
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

    private fun getNotifications (callback: (QuerySnapshot?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val userRef = db.collection("user").document(userId)

                db.collection("employees")
                    .whereEqualTo("user_ref", userRef)
                    .get()
                    .addOnSuccessListener() {document ->
                        if(document.isEmpty){
                            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
                            callback(null)
                        } else {
                            val employeeRef = document.documents.firstOrNull()?.reference

                            db.collection("visits")
                                .whereEqualTo("employee_ref", employeeRef)
                                .get()
                                .addOnSuccessListener() {document ->
                                    if(document.isEmpty){
                                        callback(null)
                                    } else {
                                        Log.i("Data", document.documents.toString())
                                        callback(document)
                                    }
                                }
                        }
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
            val notification = n.toObject(NotificationRecord::class.java)
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

            val visitorName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = n.getString("vehicleNumber")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(ResourcesCompat.getFont(this@EmpNotificationActivity, R.font.exo_2_semibold), Typeface.BOLD)
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
                setTypeface(ResourcesCompat.getFont(this@EmpNotificationActivity, R.font.lato_italic), Typeface.ITALIC)
                gravity = Gravity.CENTER
            }

            innerLayout.addView(visitorName)
            innerLayout.addView(purpose)

            val status = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setPadding(spacePadding, spacePadding, spacePadding, spacePadding)
                }
                text = "NEW"
                setTextColor(ContextCompat.getColor(context, R.color.cancel))
                textSize = 22f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            if(n.getString("status") != "Pending"){
                status.text = "\u2714"
                status.setTextColor(ContextCompat.getColor(this, R.color.approve))
            }

            n.getDocumentReference("visitor_ref")?.let { visitorRef ->
                visitorRef.get().addOnSuccessListener { visitorDoc ->
                    if (visitorDoc.exists()) {
                        val userRef = visitorDoc.getDocumentReference("user_ref")
                        if (userRef != null) {
                            getVisitorName(userRef) { name ->
                                if (name != null) {
                                    visitorName.text = name
                                    getUserProfile(visitorImage, visitorName.text.toString())
                                } else {
                                    visitorName.text = "User"
                                }
                            }
                        } else {
                            Toast.makeText(this, "No User Reference Found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Visitor Document Does Not Exist", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error Fetching Visitor Document", Toast.LENGTH_SHORT).show()
                }
            }

            recordLayout.addView(visitorImage)
            recordLayout.addView(innerLayout)
            recordLayout.addView(status)

            notificationLayout.addView(recordLayout)
        }
    }

    private fun getVisitorName(userRef: DocumentReference, callback: (String?) -> Unit){
        userRef.get()
            .addOnSuccessListener(){document ->
                if (document.exists()) {
                    val userName = document.getString("username")
                    callback(userName)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error Fetching Username", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    private fun getUserProfile(userImage: ImageView, userName: String) {
        if(checkSession()){
            val db = Firebase.firestore

            db.collection("user")
                .whereEqualTo("username", userName)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.isEmpty) {
                        val userId = document.documents[0].id
                        val storageReference = Firebase.storage.reference
                        val imageReference = storageReference.child("images/${userId}.jpg")

                        imageReference.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this@EmpNotificationActivity)
                                .load(uri)
                                .into(userImage)
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {e ->
                    Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addOnFailureListener
                }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
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