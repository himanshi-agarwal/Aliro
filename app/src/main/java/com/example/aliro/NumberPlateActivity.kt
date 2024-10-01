package com.example.aliro

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture

class NumberPlateActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 1001
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_preview)

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
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
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.cameraPreview).surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
        }, ContextCompat.getMainExecutor(this))
    }

//    private fun detectNumberPlate(image: Bitmap): Bitmap {
//        val mat = Mat()
//        Utils.bitmapToMat(image, mat)
//
//        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
//
//        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
//        Imgproc.Canny(mat, mat, 50.0, 150.0)
//
//        val contours = mutableListOf<MatOfPoint>()
//        Imgproc.findContours(mat, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
//
//        for (contour in contours) {
//            val rect = Imgproc.boundingRect(contour)
//            val aspectRatio = rect.width.toDouble() / rect.height
//            if (aspectRatio > 2 && aspectRatio < 5) {
//                val numberPlateMat = Mat(mat, rect)
//                val numberPlateBitmap = Bitmap.createBitmap(numberPlateMat.cols(), numberPlateMat.rows(), Bitmap.Config.ARGB_8888)
//                Utils.matToBitmap(numberPlateMat, numberPlateBitmap)
//                return numberPlateBitmap
//            }
//        }
//        return image
//    }
}