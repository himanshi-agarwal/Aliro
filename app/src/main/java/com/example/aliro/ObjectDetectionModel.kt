package com.example.aliro

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.io.IOException

class ObjectDetectionModel(context: Context) {
    private val OUTPUT_SIZE = 10
    private var interpreter: Interpreter? = null

    init {
        val tfliteModel = loadModelFile(context)
        interpreter = Interpreter(tfliteModel)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("object_detection.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun runInference(inputData: ByteBuffer): FloatArray {
        val output = FloatArray(OUTPUT_SIZE)
        interpreter?.run(inputData, output)
        return output
    }

    fun close() {
        interpreter?.close()
    }
}