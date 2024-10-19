package com.example.aliro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.common.primitives.Booleans
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class FaceRekognitionActivity: AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var imageCapture: ImageCapture
    private lateinit var entryButton: Button
    private lateinit var exitButton: Button
    private val CAMERA_PERMISSION_CODE = 1
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var capturedImageUri: Uri? = null
    private var userId: String? = null
    private var visitId: String? = null
    private var faceMatched: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.face_recognition)

        previewView = findViewById(R.id.cameraPreview)
        imageView = findViewById(R.id.capturedImage)
        captureButton = findViewById(R.id.capture_button)
        resultTextView = findViewById(R.id.result_text)
        entryButton = findViewById(R.id.entryButton)
        exitButton = findViewById(R.id.exitButton)

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        userId = intent.getStringExtra("userId")
        visitId = intent.getStringExtra("visitId")

        captureButton.setOnClickListener {
            takePhoto()
        }

        entryButton.setOnClickListener() {
            markEntry()
        }

        exitButton.setOnClickListener() {
            markExit()
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

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    bitmap?.let {
                        displayCapturedImage(it)
                        val uri = getImageUriFromBitmap(this@FaceRekognitionActivity, it)
                        if (uri != null) {
                            capturedImageUri = uri
                            Log.e("Take Photo", "Photo taken successfully Calling detectFaces")
                            detectFaces(it, this@FaceRekognitionActivity)
                        } else {
                            Log.e("FaceRekognitionActivity", "Failed to create URI from Bitmap")
                        }
                    } ?: run {
                        Toast.makeText(this@FaceRekognitionActivity, "Error capturing image", Toast.LENGTH_SHORT).show()
                    }
                    image.close()
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("NumberPlateActivity", "Failed to take photo", exc)
                    Toast.makeText(this@FaceRekognitionActivity, "Failed to take photo: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun displayCapturedImage(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE
        previewView.visibility = View.GONE
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

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val rotationDegrees = image.imageInfo.rotationDegrees
        return rotateBitmap(bitmap, rotationDegrees)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun detectFaces(bitmap: Bitmap, context: Context) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(highAccuracyOpts)

        Log.d("Image Info", "Image width: ${image.width}, height: ${image.height}")

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    Log.e("Detect Faces", "Face detected  successfully Calling compareWithEmployeeImage")
                    compareWithEmployeeImage(face.boundingBox, context)
                } else {
                    Toast.makeText(this, "No face detected", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error" , e.message.toString())
                Toast.makeText(this, "Error in Face Detection", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compareWithEmployeeImage(faceBoundingBox: android.graphics.Rect, context: Context) {
        if (checkSession()) {
            val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
            val id = sharedPreference.getString("userId", null)

            if (id != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${userId}.jpg")

                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.e("URI", uri.toString())
                    Log.e("Employee image retrived ", "Calling compareFaces")
                    compareFaces(capturedImageUri!!, uri, context)
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error in fetching employee image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error in User Login", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error Loading Photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compareFaces(capturedImageUri: Uri, storedImageUri: Uri, context: Context) {
        val detector = FaceDetection.getClient()

        Glide.with(context)
            .asBitmap()
            .load(capturedImageUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(capturedBitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    val capturedImage = InputImage.fromBitmap(capturedBitmap, 0)

                    detector.process(capturedImage)
                        .addOnSuccessListener { capturedFaces ->
                            if (capturedFaces.isNotEmpty()) {
                                val capturedFace = capturedFaces[0]
                                Log.d("FaceRekognitionActivity", "Captured face detected. Bounding box: ${capturedFace.boundingBox}")

                                Glide.with(context)
                                    .asBitmap()
                                    .load(storedImageUri)
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(storedBitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                            val storedImage = InputImage.fromBitmap(storedBitmap, 0)

                                            detector.process(storedImage)
                                                .addOnSuccessListener { storedFaces ->
                                                    if (storedFaces.isNotEmpty()) {
                                                        val storedFace = storedFaces[0]
                                                        Log.d("FaceRekognitionActivity", "Stored face detected. Bounding box: ${storedFace.boundingBox}")

                                                        val similarityScore = compareBoundingBoxes(capturedFace.boundingBox, storedFace.boundingBox)
                                                        if (similarityScore > 0.15) {
                                                            faceMatched = true
                                                            resultTextView.text = "Face Matched"
                                                            Toast.makeText(context, "Face Matched!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            resultTextView.text = "Face Not Matched"
                                                            Toast.makeText(context, "Face Not Matched", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Log.e("FaceRekognitionActivity", "No face detected in stored image")
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("FaceRekognitionActivity", "Error processing stored image: ${e.message}")
                                                }
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Handle placeholder if needed
                                        }
                                    })
                            } else {
                                Log.e("FaceRekognitionActivity", "No face detected in captured image")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FaceRekognitionActivity", "Error processing captured image: ${e.message}")
                        }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle placeholder if needed
                }
            })
    }

    private fun compareBoundingBoxes(box1: android.graphics.Rect, box2: android.graphics.Rect): Float {
        val widthDifference = abs(box1.width() - box2.width()).toFloat() / box1.width()
        val heightDifference = abs(box1.height() - box2.height()).toFloat() / box1.height()

        val similarityScore = 1.0f - (widthDifference + heightDifference) / 2
        Log.d("FaceRekognitionActivity", "Bounding box similarity: $similarityScore")

        return similarityScore
    }

    private fun checkSession() : Boolean{
        val sharedPreference = getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPreference.getBoolean("loggedIn", false)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    private fun markEntry() {
        if (checkSession() && faceMatched) {
            val db = Firebase.firestore
            if (visitId != null) {
                val entryTime = FieldValue.serverTimestamp()

                db.collection("visits")
                    .document(visitId!!)
                    .update(mapOf(
                        "checkInTime" to entryTime,
                        "status" to "Approved"
                    ))
                    .addOnSuccessListener {
                        val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(entryTime)
                        resultTextView.text = "Entry marked at: $formattedTime"
                        Toast.makeText(this, "Entry marked successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to mark entry: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("Firestore", "Error updating entryTime", e)
                    }
            } else {
                Toast.makeText(this, "Invalid visit ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Face Not Matched", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markExit() {
        if (checkSession()) {
            val db = Firebase.firestore
            if (visitId != null) {
                val exitTime = FieldValue.serverTimestamp()

                db.collection("visits")
                    .document(visitId!!)
                    .update("checkOutTime", exitTime)
                    .addOnSuccessListener {
                        val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(exitTime)
                        resultTextView.text = "Exit marked at: $formattedTime"
                        Toast.makeText(this, "Exit marked successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to mark entry: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("Firestore", "Error updating entryTime", e)
                    }
            } else {
                Toast.makeText(this, "Invalid visit ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error in User Session", Toast.LENGTH_SHORT).show()
        }
    }
}