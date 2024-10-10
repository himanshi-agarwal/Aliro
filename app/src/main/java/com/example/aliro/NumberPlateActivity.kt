package com.example.aliro

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.storage.FirebaseStorage
import com.googlecode.tesseract.android.TessBaseAPI
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer

class NumberPlateActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var answer: TextView
    private lateinit var imageCapture: ImageCapture
    private lateinit var tessBaseAPI: TessBaseAPI
    private val CAMERA_PERMISSION_CODE = 1001
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var croppedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_preview)

        previewView = findViewById(R.id.cameraPreview)
        captureButton = findViewById(R.id.captureButton)
        answer = findViewById(R.id.numberPlate)

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        captureButton.setOnClickListener(){
            takePhoto()
        }

        tessBaseAPI = TessBaseAPI()
        val tessDataPath = File(filesDir, "tesseract")
        if (!tessDataPath.exists()) {
            tessDataPath.mkdirs()
        }

        val tessDataFile = File(tessDataPath, "eng.traineddata")
        if (!tessDataFile.exists()) {
            Toast.makeText(this, "OCR data not found!", Toast.LENGTH_LONG).show()
            return
        }

        tessBaseAPI.init(tessDataPath.absolutePath, "eng")
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer: ByteBuffer = planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()

                    sendImageToAPI(bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("NumberPlateActivity", "Failed to take photo", exc)
                    Toast.makeText(this@NumberPlateActivity, "Failed to take photo: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun sendImageToAPI(bitmap: Bitmap) {
        val API_URL = "https://api-inference.huggingface.co/models/nickmuchi/yolos-small-finetuned-license-plate-detection"
        val API_KEY = "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"

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        val client = OkHttpClient()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageData)

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@NumberPlateActivity, "API request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Log.e("NumberPlateActivity", "Request failed with status code: ${response.code}")
                            Toast.makeText(this@NumberPlateActivity, "Request failed with status code: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val jsonResponse = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(this@NumberPlateActivity, "Response: $jsonResponse", Toast.LENGTH_SHORT).show()
                            val boundingBox = parseBoundingBox(jsonResponse!!)

                            val xmin = boundingBox.left
                            val ymin = boundingBox.top
                            val xmax = boundingBox.right
                            val ymax = boundingBox.bottom

                            croppedBitmap = cropImage(bitmap, xmin, xmax, ymin, ymax)

                            uploadCroppedImageToFirebase(croppedBitmap!!)

                            answer.text = jsonResponse
                            Log.i("Plate", jsonResponse)
                        }
                    }
                }
            }
        })
    }

    private fun parseBoundingBox(jsonResponse: String): Rect {
        val jsonArray = JSONArray(jsonResponse)
        val firstObject = jsonArray.getJSONObject(0)
        val boxObject = firstObject.getJSONObject("box")

        val xmin = boxObject.getInt("xmin")
        val ymin = boxObject.getInt("ymin")
        val xmax = boxObject.getInt("xmax")
        val ymax = boxObject.getInt("ymax")

        return Rect(xmin, ymin, xmax, ymax)
    }

    private fun cropImage(originalBitmap: Bitmap, xmin: Int, xmax: Int, ymin: Int, ymax: Int): Bitmap {
        val width = xmax - xmin
        val height = ymax - ymin
        return Bitmap.createBitmap(originalBitmap, xmin, ymin, width, height)
    }

    private fun uploadCroppedImageToFirebase(croppedBitmap: Bitmap) {
        if (checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val userID = sharedPreference.getString("userId", null)

            if (userID != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/number_plate_images/${userID}.jpg")

                val baos = ByteArrayOutputStream()
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()

                val uploadTask = imageRef.putBytes(imageData)
                uploadTask.addOnSuccessListener {
                    Toast.makeText(this, "Cropped image uploaded successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Failed to upload image: ${exception.message}")
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error Loading User Id", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSession(): Boolean {
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.contains("userId")
    }
}