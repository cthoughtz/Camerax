package com.raywenderlich.colorcam

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class LuminosityAnalyzer: ImageAnalysis.Analyzer {

    private var lastTimeStamp = 0L
    private val TAG = this.javaClass.simpleName


    override fun analyze(image: ImageProxy, rotationDegrees: Int) {

        val currentTimeStamp = System.currentTimeMillis()
        val intervalInSeconds = TimeUnit.SECONDS.toMillis(1)
        val deltaTime = currentTimeStamp - lastTimeStamp

        if (deltaTime >= intervalInSeconds){

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            lastTimeStamp = currentTimeStamp
            Log.d(TAG, "Average luminosity: $luma")
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray{

        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }


}