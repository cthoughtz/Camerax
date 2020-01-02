package com.raywenderlich.colorcam

import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.image
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File
import java.util.concurrent.Executors
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

  companion object{

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private const val PERMISSIONS_CODE =1
    private const val FILE_NAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val PHOTO_EXTENSION = ".jpg"
  }

  private var lensFacing = CameraX.LensFacing.BACK
  private val executor = Executors.newSingleThreadExecutor()
  private var flashMode = FlashMode.OFF

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestCameraPermissions()
    setFlipButtonListener()
    setFlashModeListener()
  }

  private fun hasAllPermissions() = REQUIRED_PERMISSIONS.all {

    ContextCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED
  }

  private fun requestCameraPermissions(){

    if(hasAllPermissions()){
      textureView.post {
        bindCamera()
      }
    } else{
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_CODE)
    }
  }

  private fun bindCamera(){

    CameraX.unbindAll()

    val previewUseCase = createPreviewUseCase()
    val imageCapture = createImageCaptureUseCase()
    val luminosityAnalyzer = createLuminosityAnalyzer()

    CameraX.bindToLifecycle(this, previewUseCase, imageCapture, luminosityAnalyzer)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    if (requestCode == PERMISSIONS_CODE){
      if (hasAllPermissions()){
        textureView.post {
          bindCamera()
        }
      } else{
        longToast("Camera Permission not Granted")
        finish()
      }
    }
  }

  private fun updateTextureView(){

    val centerY = textureView.height / 2f
    val centerX = textureView.width / 2f
    val matrix = Matrix()

    val rotationDegree = when(textureView.display.rotation){

      Surface.ROTATION_0 -> 0
      Surface.ROTATION_90 -> 90
      Surface.ROTATION_180 -> 180
      Surface.ROTATION_270 -> 270
      else -> return
    }

    matrix.postRotate(-rotationDegree.toFloat(), centerX,centerY)
    textureView.setTransform(matrix)
  }

  fun createPreviewUseCase(): Preview{

    val previewConfig = PreviewConfig.Builder().apply {

      setTargetResolution(Size(textureView.width, textureView.height))
      setLensFacing(lensFacing)
    }.build()

    val preview = Preview(previewConfig)

    preview.setOnPreviewOutputUpdateListener{

      val parent = textureView.parent as ViewGroup
      parent.removeView(textureView)
      parent.addView(textureView, 0)
      textureView.surfaceTexture = it.surfaceTexture
      updateTextureView()
    }

    return preview
  }

  private fun setFlipButtonListener(){

    flipButton.setOnClickListener {

      if (CameraX.LensFacing.FRONT == lensFacing){

        lensFacing = CameraX.LensFacing.BACK
        flipButton.setImageDrawable(getDrawable(R.drawable.ic_camera_front_black_24dp))
      }else{
        lensFacing = CameraX.LensFacing.FRONT
        flipButton.setImageDrawable(getDrawable(R.drawable.ic_camera_rear_black_48dp))
      }
    }
  }

  private fun createImageCaptureUseCase(): ImageCapture{

    val outputDirectory = getOutputDirectory(this)

    val imageCaptureConfig = ImageCaptureConfig.Builder().apply {

      setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
      setLensFacing(lensFacing)
      setFlashMode(flashMode)
    }.build()

    val imageCapture = ImageCapture(imageCaptureConfig)

    captureButton.setOnClickListener {

      val photoFile = createNewFile(outputDirectory, FILE_NAME, PHOTO_EXTENSION)
      imageCapture.takePicture(photoFile, executor, object: ImageCapture.OnImageSavedListener{
        override fun onImageSaved(file: File) {

          textureView.post {
            toast("Photo saved: ${file.absolutePath}")
          }
        }

        override fun onError(
          imageCaptureError: ImageCapture.ImageCaptureError, message: String, cause: Throwable?) {

          textureView.post {
            toast("Photo capture failed: $message")
          }
        }
      })

    }

    return imageCapture
  }

  private fun setFlashModeListener(){

    flashButton.setOnClickListener {

      if (FlashMode.OFF == flashMode){
        flashMode = FlashMode.ON
        flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_on_black_48dp))
      }else{
        flashButton.setImageDrawable(getDrawable(R.drawable.ic_flash_off_black_48dp))
        flashMode = FlashMode.OFF
      }
      bindCamera()
    }
  }

  private fun createLuminosityAnalyzer(): ImageAnalysis{

    val analyzerConfig = ImageAnalysisConfig.Builder().apply {

      setLensFacing(lensFacing)
      setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
    }.build()

    val analyzer = ImageAnalysis(analyzerConfig).apply {

      setAnalyzer(executor, LuminosityAnalyzer())
    }

    return analyzer
  }
}






