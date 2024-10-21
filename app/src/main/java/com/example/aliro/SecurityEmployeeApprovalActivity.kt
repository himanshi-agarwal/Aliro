package com.example.aliro

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

class SecurityEmployeeApprovalActivity: AppCompatActivity() {
    private lateinit var searchBar: SearchView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.security_employee_approval)

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
                    Toast.makeText(applicationContext, "Scan Employee's Face", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SecurityEmployeeApprovalActivity::class.java)
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
                    logout()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        searchBar = findViewById(R.id.serachBar)
        val searchEditText = searchBar.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                getEmployees(query) { employees ->
                    if (employees != null) {
                        showEmployeeRecords(employees)
                    } else {
                        Toast.makeText(this@SecurityEmployeeApprovalActivity, "No records found", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                getEmployees(newText) { employees ->
                    if (employees != null) {
                        showEmployeeRecords(employees)
                    } else {
                        Toast.makeText(this@SecurityEmployeeApprovalActivity, "No records found", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        })

        getEmployees("") { employees ->
            if(employees != null){
                showEmployeeRecords(employees)
            } else {
                Toast.makeText(this, "No Employees Data Found", Toast.LENGTH_SHORT).show()
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

    private fun getEmployees (query: String?, callback: (QuerySnapshot?) -> Unit) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if(userId != null){
                val db = Firebase.firestore
                val employeeRef = db.collection("employees")

                val employeeQuery = if (!query.isNullOrEmpty()) {
                    employeeRef
                        .orderBy("FirstName")
                        .startAt(query)
                        .endAt(query + "\uf8ff")
                } else {
                    employeeRef
                        .orderBy("EmpID")
                }

                employeeQuery
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.isEmpty) {
                            callback(null)
                        } else {
                            callback(document)
                        }
                    }
                    .addOnFailureListener { e ->
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

    private fun showEmployeeRecords(employees: QuerySnapshot) {
        val visitsLayout = findViewById<LinearLayout>(R.id.employeeParent)
        visitsLayout.removeAllViews()

        val notificationPadding = resources.getDimensionPixelSize(R.dimen.record_padding)
        val imageDimension = resources.getDimensionPixelSize(R.dimen.image_dimen)
        val imageMarginEnd = resources.getDimensionPixelSize(R.dimen.image_margin)
        val spacePadding = resources.getDimensionPixelSize(R.dimen.space_padding)

        for (e in employees){
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

            val employeeImage = ImageView(this).apply {
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

            val employeeName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = e.getString("FirstName") + " " + e.getString("LastName")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 22f
                setTypeface(ResourcesCompat.getFont(this@SecurityEmployeeApprovalActivity, R.font.exo_2_semibold), Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val company = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = e.getString("Company")
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(ResourcesCompat.getFont(this@SecurityEmployeeApprovalActivity, R.font.lato_italic), Typeface.ITALIC)
                gravity = Gravity.CENTER
            }

            e.getDocumentReference("user_ref")?.let { userRef ->
                if (userRef != null) {
                    getUserProfile(employeeImage, userRef)
                    recordLayout.setOnClickListener {
                        val intent = Intent(this, FaceRekognitionActivity::class.java)
                        intent.putExtra("userId", userRef.id)
                        intent.putExtra("visitId", e.id)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "No User Reference Found", Toast.LENGTH_SHORT).show()
                }
            }

            innerLayout.addView(employeeName)
            innerLayout.addView(company)

            recordLayout.addView(employeeImage)
            recordLayout.addView(innerLayout)

            visitsLayout.addView(recordLayout)
        }
    }

    private fun getUserProfile(userImage: ImageView, userRef: DocumentReference) {
        if(checkSession()){
            val userId = userRef.id

            val storageReference = Firebase.storage.reference
            val imageReference = storageReference.child("images/${userId}.jpg")

            imageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@SecurityEmployeeApprovalActivity)
                    .load(uri)
                    .into(userImage)
            }.addOnFailureListener {
                Log.e("Image", "Failed to load Image")
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

            Toast.makeText(applicationContext, "Logout Successfully", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No") {
                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}