package com.example.aliro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Calendar

class VisitorRegisterActivity : AppCompatActivity() {
    private lateinit var toolbar : Toolbar
    private lateinit var name: EditText
    private lateinit var phone_no: EditText
    private lateinit var email: EditText
    private lateinit var empID: EditText
    private lateinit var empName: EditText
    private lateinit var empCompany: EditText
    private lateinit var purpose: EditText
    private lateinit var visitDateButton: Button
    private lateinit var visitDate: TextView
    private lateinit var uploadPhoto: ImageView
    private lateinit var clickPhoto: ImageView
    private lateinit var registerButton: Button

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile -> {
                Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.about -> {
                Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AboutActivity::class.java)
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
        setContentView(R.layout.vis_register)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        name = findViewById(R.id.fullname)
        phone_no = findViewById(R.id.phone_no)
        email = findViewById(R.id.email)
        empID = findViewById(R.id.empid)
        empName = findViewById(R.id.empname)
        empCompany = findViewById(R.id.empcompany)
        purpose = findViewById(R.id.purpose)
        visitDateButton = findViewById(R.id.visitDateButton)
        visitDate = findViewById(R.id.visitDate)
        uploadPhoto = findViewById(R.id.uploadPhoto)
        clickPhoto = findViewById(R.id.clickPhoto)
        registerButton = findViewById(R.id.register_button)

        visitDateButton.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this, { view, year, monthOfYear, dayOfMonth ->
                    Log.i("Date", dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    visitDateButton.visibility = View.GONE
                    visitDate.visibility = View.VISIBLE
                    visitDate.text = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                }, year, month, day
            )
            datePickerDialog.show()
        }

        registerButton.setOnClickListener(){
            checkDetails()
        }
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun checkDetails(): Boolean{
        val visitorName = name.text.toString()
        val visitorPhoneNo = phone_no.text.toString()
        val visitorEmail = email.text.toString()
        val empId = empID.text.toString()
        val empName = empName.text.toString()
        val empCompany = empCompany.text.toString()
        val purpose = purpose.text.toString()
        val visitDate = visitDate.text.toString()

        if(visitorName.isBlank() || visitorPhoneNo.isBlank() || visitorEmail.isBlank()){
            Toast.makeText(this, "Visitor Details Incomplete", Toast.LENGTH_SHORT).show()
            return false
        }

        if(empId.isBlank() || empName.isBlank() || empCompany.isBlank()){
            Toast.makeText(this, "Employee Details Incomplete", Toast.LENGTH_SHORT).show()
            return false
        }

        if(purpose.isBlank() || visitDate.isBlank()){
            Toast.makeText(this, "Please Provide a Purpose", Toast.LENGTH_SHORT).show()
            return false
        }

        registerVisit(empId)
        return true
    }

    private fun registerVisit(empId: String) {
        if(checkSession()){
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPreference.getString("userId", null)

            if (userId != null){
                val db = Firebase.firestore

                val userRef = db.collection("user").document(userId)
                getVisitor(userId) { visitorRef ->
                    if(visitorRef == null){
                        Toast.makeText(this, "Visitor not found", Toast.LENGTH_SHORT).show()
                        return@getVisitor
                    }

                    getEmployee(empId) {employeeRef ->
                        if(employeeRef == null){
                            Toast.makeText(this, "Visitor not found", Toast.LENGTH_SHORT).show()
                            return@getEmployee
                        }

                        

                    }
                }

            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVisitor(userId : String, callback: (String?) -> Unit) {
        val db = Firebase.firestore

        db.collection("visitors")
            .whereEqualTo("user_ref", userId)
            .get()
            .addOnSuccessListener(){document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Incorrect Visitor Details", Toast.LENGTH_SHORT).show()
                    callback(null)
                } else {
                    val visitorRef = document.documents.firstOrNull()?.id
                    callback(visitorRef) // Return the first visitor ID or null if no documents
                }
            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error in Search", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    private fun getEmployee(empId: String, callback: (String?) -> Unit) {
        val db = Firebase.firestore

        db.collection("employees")
            .whereEqualTo("EmpID", empId.toInt())
            .get()
            .addOnSuccessListener(){document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Incorrect Employee Details", Toast.LENGTH_SHORT).show()
                    callback(null)
                } else {
                    val empRef = document.documents.firstOrNull()?.id
                    callback(empRef)
                }
            }
            .addOnFailureListener(){
                Toast.makeText(this, "Error in Search", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}