package com.example.aliro

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class SecurityVisitorsApprovalActivity: AppCompatActivity() {
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.security_visitors_approval)

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
                    val intent = Intent(this, SecurityHomeActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
                }

                R.id.employee -> {
                    Toast.makeText(applicationContext, "Scan Employees's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FaceRekognitionActivity::class.java)
                    startActivity(intent)
                }

                R.id.visitors -> {
                    Toast.makeText(applicationContext, "Scan Visitor's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityHomeActivity::class.java)
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

        getNotifications { notifications ->
            if(notifications != null){
                showNotifications(notifications)
            } else {
                createNoNotificationsLayout()
                Toast.makeText(this, "No Pending Request Found", Toast.LENGTH_SHORT).show()
            }
        }

        updateSidebarHeader()
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

    private fun getNotifications (callback: (QuerySnapshot?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore

                db.collection("visits")
                    .whereEqualTo("status", "Pending")
                    .get()
                    .addOnSuccessListener() {document ->
                        if(document.isEmpty){
                            callback(null)
                        } else {
                            callback(document)
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
            Toast.makeText(this, "Error User Session", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun showNotifications(visits: QuerySnapshot) {
        val visitsLayout = findViewById<LinearLayout>(R.id.visitsParent)
        visitsLayout.removeAllViews()

        val notificationPadding = resources.getDimensionPixelSize(R.dimen.record_padding)
        val imageDimension = resources.getDimensionPixelSize(R.dimen.image_dimen)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin)
        val spacePadding = resources.getDimensionPixelSize(R.dimen.space_padding)

        for (v in visits){
            val recordLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(notificationPadding, notificationPadding, notificationPadding, notificationPadding)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                orientation = LinearLayout.HORIZONTAL
                isClickable = true
                isFocusable = true
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
                text = "Name"
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(ResourcesCompat.getFont(this@SecurityVisitorsApprovalActivity, R.font.exo_2_semibold), Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val purpose = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = v.getString("visitPurpose")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(ResourcesCompat.getFont(this@SecurityVisitorsApprovalActivity, R.font.lato_italic), Typeface.ITALIC)
                gravity = Gravity.CENTER
            }

            innerLayout.addView(visitorName)
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

            v.getDocumentReference("visitor_ref")?.let { visitorRef ->
                visitorRef.get().addOnSuccessListener { visitorDoc ->
                    if (visitorDoc.exists()) {
                        val userRef = visitorDoc.getDocumentReference("user_ref")
                        if (userRef != null) {
                            getVisitorName(userRef) { name ->
                                if (name != null) {
                                    visitorName.text = name
                                    getUserProfile(visitorImage, visitorName.text.toString())
                                    recordLayout.setOnClickListener {
                                        val intent = Intent(this, FaceRekognitionActivity::class.java)
                                        intent.putExtra("userId", userRef.id)
                                        intent.putExtra("visitId", v.id)
                                        startActivity(intent)
                                    }
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
            recordLayout.addView(statusImage)

            visitsLayout.addView(recordLayout)
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
                            Glide.with(this@SecurityVisitorsApprovalActivity)
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

    private fun createNoNotificationsLayout() {
        val visitsLayout = findViewById<LinearLayout>(R.id.visitsParent)
        visitsLayout.removeAllViews()

        val imageHeight = resources.getDimensionPixelSize(R.dimen.imageHeight)

        val constraintLayout = ConstraintLayout(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(R.color.background, null)) // Set your background color
        }

        val imageView = ImageView(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                imageHeight
            ).apply {
                marginStart = 10
                marginEnd = 10
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            }
            setImageResource(R.drawable.bell)
        }

        val textView = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = imageView.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            }
            text = "Nothing Here !!!"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            textSize = 40f
            typeface = ResourcesCompat.getFont(this@SecurityVisitorsApprovalActivity, R.font.bangers)
        }

        val textView2 = TextView(this).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToBottom = textView.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            }
            text = "No Notifications to View or Approve"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            textSize = 20f
            typeface = ResourcesCompat.getFont(this@SecurityVisitorsApprovalActivity, R.font.lato_italic)
        }

        constraintLayout.addView(imageView)
        constraintLayout.addView(textView)
        constraintLayout.addView(textView2)

        visitsLayout.addView(constraintLayout)
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