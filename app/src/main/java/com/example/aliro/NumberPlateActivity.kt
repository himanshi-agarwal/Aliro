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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import com.googlecode.tesseract.android.TessBaseAPI

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
        cameraProviderFuture.addListener(Runnable {
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

    private fun takePhoto() {
        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("NumberPlateActivity", "Failed to take photo", exc)
                    Toast.makeText(this@NumberPlateActivity, "Failed to take photo: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("NumberPlateActivity", "onImageSaved: Photo saved successfully: ${photoFile.absolutePath}")
                    Toast.makeText(this@NumberPlateActivity, "Photo captured: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()

//                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
//                    val extractedText = performOCR(bitmap)
//                    Log.d("OCRActivity", "Extracted Text: $extractedText")
//
//                    answer.text = extractedText

                    sendImageToAPI(photoFile)
                }
            }
        )
    }

    private fun performOCR(bitmap: Bitmap): String {
        tessBaseAPI.setImage(bitmap)
        return tessBaseAPI.utF8Text
    }

    override fun onDestroy() {
        super.onDestroy()
        tessBaseAPI.end()
    }

    private fun sendImageToAPI(imageFile: File) {
        val API_URL = "https://api-inference.huggingface.co/models/nickmuchi/yolos-small-finetuned-license-plate-detection"
        val API_KEY = "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" // Replace with your actual key

        val client = OkHttpClient()

        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

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
                            Log.e("API", "${response.code}")
                        }
                    } else {
                        val jsonResponse = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(this@NumberPlateActivity, "Response: $jsonResponse", Toast.LENGTH_SHORT).show()
                            answer.text = jsonResponse
                            if (jsonResponse != null) {
                                Log.i("Plate", jsonResponse)
                            }
                        }
                    }
                }
            }
        })
    }
}