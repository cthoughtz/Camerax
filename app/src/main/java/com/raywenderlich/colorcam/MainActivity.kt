package com.raywenderlich.colorcam

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

  companion object{

    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private const val PERMISSIONS_CODE =1
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestCameraPermissions()
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
    CameraX.bindToLifecycle(this)
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
}

