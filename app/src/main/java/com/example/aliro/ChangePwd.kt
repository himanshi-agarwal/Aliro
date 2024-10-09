package com.example.aliro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePwd : AppCompatActivity() {

    private lateinit var etRegistration: EditText
    private lateinit var btnGetOtp: Button
    private lateinit var otpBox1: EditText
    private lateinit var otpBox2: EditText
    private lateinit var otpBox3: EditText
    private lateinit var otpBox4: EditText
    private lateinit var btnValidate: Button
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_pwd)


        etRegistration = findViewById(R.id.et_registration)
        btnGetOtp = findViewById(R.id.btn_get_otp)
        otpBox1 = findViewById(R.id.otp_box_1)
        otpBox2 = findViewById(R.id.otp_box_2)
        otpBox3 = findViewById(R.id.otp_box_3)
        otpBox4 = findViewById(R.id.otp_box_4)
        btnValidate = findViewById(R.id.btn_validate)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnSubmit = findViewById(R.id.btn_submit)


        setUpOtpAutoMove()


        btnGetOtp.setOnClickListener {
            val registrationNumber = etRegistration.text.toString()
            // Validate registration number and send OTP
            if (registrationNumber.isNotEmpty()) {

                showMessage("OTP sent to registered number.")
            } else {
                showMessage("Please enter registration number.")
            }
        }


        btnValidate.setOnClickListener {
            val otp = otpBox1.text.toString() + otpBox2.text.toString() + otpBox3.text.toString() + otpBox4.text.toString()
            if (otp.length == 4) {
                // TODO: Implement OTP validation logic
                showMessage("OTP validated.")
            } else {
                showMessage("Please enter a valid 4-digit OTP.")
            }
        }


        btnSubmit.setOnClickListener {
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (newPassword == confirmPassword) {
                    // TODO: Implement password change logic
                    showMessage("Password changed successfully.")
                } else {
                    showMessage("Passwords do not match.")
                }
            } else {
                showMessage("Please enter and confirm your new password.")
            }
        }
    }


    private fun showMessage(message: String) {
        // For simplicity, using Toast here
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Set up auto move for OTP input fields
    private fun setUpOtpAutoMove() {
        val otpFields = listOf(otpBox1, otpBox2, otpBox3, otpBox4)

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()  // Move to next box
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus()  // Move to previous box
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}
