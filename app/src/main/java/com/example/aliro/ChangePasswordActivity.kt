package com.example.aliro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import kotlin.random.Random

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var mobileNo: EditText
    private lateinit var otpButton: Button
    private lateinit var otpBox1: EditText
    private lateinit var otpBox2: EditText
    private lateinit var otpBox3: EditText
    private lateinit var otpBox4: EditText
    private lateinit var btnValidate: Button
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    val PERMISSION_REQUEST_CODE = 1
    private var otp: Int = generateOTP()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_pwd)
        enableEdgeToEdge()

        mobileNo = findViewById(R.id.registration_mobile)
        otpButton = findViewById(R.id.OTP_btn)
        otpBox1 = findViewById(R.id.otp_box_1)
        otpBox2 = findViewById(R.id.otp_box_2)
        otpBox3 = findViewById(R.id.otp_box_3)
        otpBox4 = findViewById(R.id.otp_box_4)
        btnValidate = findViewById(R.id.validate_btn)
        newPassword = findViewById(R.id.new_password)
        confirmPassword = findViewById(R.id.confirm_password)
        saveButton = findViewById(R.id.save)
        cancelButton = findViewById(R.id.cancel)

        setUpOtpAutoMove()

        otpBox1.visibility = View.GONE
        otpBox2.visibility = View.GONE
        otpBox3.visibility = View.GONE
        otpBox4.visibility = View.GONE
        btnValidate.visibility = View.GONE
        newPassword.visibility = View.GONE
        confirmPassword.visibility = View.GONE
        saveButton.visibility = View.GONE
        cancelButton.visibility = View.GONE

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), PERMISSION_REQUEST_CODE)
        }

        otpButton.setOnClickListener {
            val registeredNumber = mobileNo.text.toString()
            verifyMobileNumber(registeredNumber)
        }

        btnValidate.setOnClickListener {
            val enteredOTP = otpBox1.text.toString() + otpBox2.text.toString() + otpBox3.text.toString() + otpBox4.text.toString()
            if (enteredOTP.length == 4) {
                verifyOTP(enteredOTP.toInt())
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val newPassword = newPassword.text.toString()
            val confirmPassword = confirmPassword.text.toString()
        }

        cancelButton.setOnClickListener() {
            finish()
        }
    }

    private fun setUpOtpAutoMove() {
        val otpFields = listOf(otpBox1, otpBox2, otpBox3, otpBox4)

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun verifyMobileNumber(registeredNumber: String) {
        val db = Firebase.firestore

        db.collection("employees")
            .whereEqualTo("Phone_Number", registeredNumber)
            .get()
            .addOnSuccessListener() {document ->
                if(document.isEmpty){
                    Toast.makeText(this, "Not a Registered Mobile Number", Toast.LENGTH_SHORT).show()
                } else {
                    sendOTP(registeredNumber)
                }
            }
            .addOnFailureListener() {
                Toast.makeText(this, "Error in Fetching Record", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateOTP(): Int {
        val otp = Random.nextInt(1000, 10000)
        Log.i("OTP", otp.toString())
        return otp
    }

    private fun sendOTP(registeredNumber: String) {
        val message = "Your one-time password (OTP) for Password Change is $otp. This OTP is valid for the next 10 minutes. Please enter it in the app or website to complete your verification process."
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(registeredNumber, null, message, null, null)
            Toast.makeText(this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()

            otpBox1.visibility = View.VISIBLE
            otpBox2.visibility = View.VISIBLE
            otpBox3.visibility = View.VISIBLE
            otpBox4.visibility = View.VISIBLE
            btnValidate.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "SMS Failed to Send", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun verifyOTP(enteredOTP: Int) {
        if(enteredOTP == otp){
            Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show()
            otpBox1.visibility = View.GONE
            otpBox2.visibility = View.GONE
            otpBox3.visibility = View.GONE
            otpBox4.visibility = View.GONE
            btnValidate.visibility = View.GONE
            newPassword.visibility = View.VISIBLE
            confirmPassword.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
        }
    }
}