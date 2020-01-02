package com.raywenderlich.colorcam

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.lang.Exception
import java.util.concurrent.TimeUnit

class QRCodeAnalyzer: ImageAnalysis.Analyzer {

    companion object{

        private const val TAG = "ColorCam"
    }

    private var lastAnalyzedTimeStamp = 0L

    override fun analyze(imageProxy: ImageProxy, rotationDegrees: Int) {

        val currentTimeStamp = System.currentTimeMillis()
        val intervalInSeconds = TimeUnit.SECONDS.toMillis(10)
        val deltaTime = currentTimeStamp - lastAnalyzedTimeStamp

        if (deltaTime >= intervalInSeconds){

            val mediaImage = imageProxy.image
            val imageRotation = degreesToFirebaseRotation(rotationDegrees)

            if (mediaImage != null){

                val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
                val detector = FirebaseVision.getInstance().visionBarcodeDetector
                detector.detectInImage(image).addOnSuccessListener {barcodes ->

                    for (barcode in barcodes){
                        when(barcode.valueType){

                            FirebaseVisionBarcode.TYPE_WIFI ->{

                                val ssid = barcode.wifi!!.ssid
                                val password = barcode.wifi!!.password
                                val type = barcode.wifi!!.encryptionType
                                Log.d(TAG, "$ssid $password $type")
                            }

                            FirebaseVisionBarcode.TYPE_URL -> {

                                Log.d(TAG, "URL: ${barcode.rawValue}")
                            }
                        }
                    }
                }
                    .addOnFailureListener{
                        Log.d(TAG,"Error: ${it.message}")
                    }
            }
        }
        lastAnalyzedTimeStamp = currentTimeStamp
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees){

        0-> FirebaseVisionImageMetadata.ROTATION_0
        90-> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, 270.")
    }
}