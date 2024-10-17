package com.example.aliro

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.storage

data class diaryRecord(
    val userRef: String = "",
    val date: Timestamp? = null,
    val message: String = "",
)

class VisitorDiaryActivity : AppCompatActivity(){
    private lateinit var diaryMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var toolbar : Toolbar
    private lateinit var diaryParentLayout : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.visitor_diary)

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

        diaryParentLayout = findViewById(R.id.diaryParentLayout)

        getDiaryRecord { records ->
            if (records != null) {
                displayDiaryRecords(records)
            } else {
                Toast.makeText(this, "No Records Available", Toast.LENGTH_SHORT).show()
            }
        }

        diaryMessage = findViewById(R.id.diaryMessage)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener(){
            saveDiaryContent()
        }
    }

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

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun getDiaryRecord(callback: (QuerySnapshot?) -> Unit){
        if (checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore

                db.collection("diary")
                    .get()
                    .addOnSuccessListener {document ->
                        if (document.isEmpty){
                            callback(null)
                        }
                        else{
                            callback(document)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error in Diary", Toast.LENGTH_SHORT).show()
                        Log.w("Firestore", "Error getting documents: ", exception)
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUsername(userRef: DocumentReference, callback: (String?) -> Unit){
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

    private fun displayDiaryRecords(record: QuerySnapshot){
        diaryParentLayout.removeAllViews()

        val diaryBoxMarginHorizontal = resources.getDimensionPixelSize(R.dimen.diaryBoxMarginHorizontal)
        val diaryBoxMarginVertical = resources.getDimensionPixelSize(R.dimen.diaryBoxMarginVertical)
        val diaryBoxPadding = resources.getDimensionPixelSize(R.dimen.diaryBoxPadding)
        val cardView = resources.getDimensionPixelSize(R.dimen.cardView)
        val userMarginStart = resources.getDimensionPixelSize(R.dimen.userMarginStart)
        val userPaddingStart = resources.getDimensionPixelSize(R.dimen.userPaddingStart)

        for (r in record){
            val rootLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(diaryBoxMarginVertical, diaryBoxMarginHorizontal, diaryBoxMarginVertical, diaryBoxMarginHorizontal)
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(diaryBoxPadding, diaryBoxPadding, diaryBoxPadding, diaryBoxPadding)
                background = ContextCompat.getDrawable(this@VisitorDiaryActivity, R.drawable.rounded_corner)
            }

            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    cardView,
                    cardView
                )
                radius = 150f
                cardElevation = 0f
            }

            val imageView = ImageView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setImageResource(R.drawable.account)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = ContextCompat.getDrawable(this@VisitorDiaryActivity, R.drawable.circular_background)
            }
            cardView.addView(imageView)

            val textLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                    setMargins(userMarginStart, 0, 0, 0)
                }
                orientation = LinearLayout.VERTICAL
                setPadding(userPaddingStart, 0, 0, 0)
            }

            val nameTextView = TextView(this).apply {
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTypeface(ResourcesCompat.getFont(this@VisitorDiaryActivity, R.font.lato_italic), Typeface.BOLD)
            }

            r.getDocumentReference("user_ref")?.let {userRef ->
                getUsername(userRef){username ->
                    if(username != null){
                        nameTextView.text = username
                        getUserProfile(imageView, nameTextView.text.toString())
                    }
                    else{
                        nameTextView.text = "User"
                    }
                }
            }

            val descriptionTextView = TextView(this).apply {
                text = r.getString("message")
                textSize = 10f
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setTypeface(ResourcesCompat.getFont(this@VisitorDiaryActivity, R.font.inter_medium), Typeface.BOLD)
            }

            textLayout.addView(nameTextView)
            textLayout.addView(descriptionTextView)

            rootLayout.addView(cardView)
            rootLayout.addView(textLayout)

            diaryParentLayout.addView(rootLayout)
        }
    }

    private fun saveDiaryContent() {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore
                val currentDate = Timestamp.now()
                val message = diaryMessage.text.toString()
                val userDocRef = db.collection("user").document(userId)

                val diaryHashmap = hashMapOf(
                    "date" to currentDate,
                    "message" to message,
                    "user_ref" to userDocRef
                )

                val diaryDocRef = db.collection("diary").document()

                diaryDocRef.set(diaryHashmap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Review Added Successfully", Toast.LENGTH_SHORT).show()
                        diaryMessage.text.clear()
                        val intent = Intent(this, VisitorDiaryActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error in Adding Review", Toast.LENGTH_SHORT).show()
                        return@addOnFailureListener
                    }

            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
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
                            Glide.with(this@VisitorDiaryActivity)
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
}