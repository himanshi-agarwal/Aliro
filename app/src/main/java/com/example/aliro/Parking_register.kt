package com.example.aliro

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Parking_register : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editVehicleNumber: EditText
    private lateinit var vehicleTypeGroup: RadioGroup
    private lateinit var buttonSubmit: Button
    private lateinit var parkingSpaceInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parking_register) // Ensure this matches your layout file name


        editName = findViewById(R.id.edit_name)
        editVehicleNumber = findViewById(R.id.edit_vehicle_number)
        vehicleTypeGroup = findViewById(R.id.vehicle_type_group)
        buttonSubmit = findViewById(R.id.button_submit)
        parkingSpaceInfo = findViewById(R.id.parking_space_info)


        buttonSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {

        val name = editName.text.toString()
        val vehicleNumber = editVehicleNumber.text.toString()
        val selectedVehicleTypeId = vehicleTypeGroup.checkedRadioButtonId
        val vehicleType = findViewById<RadioButton>(selectedVehicleTypeId).text.toString()


        val parkingInfo = "Parking space allotted for $name ($vehicleType) with vehicle number $vehicleNumber"
        parkingSpaceInfo.text = parkingInfo
        parkingSpaceInfo.visibility = TextView.VISIBLE
    }
}
