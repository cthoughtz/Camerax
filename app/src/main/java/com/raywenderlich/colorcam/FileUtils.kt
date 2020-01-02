package com.raywenderlich.colorcam

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun createNewFile(folder: File, format: String, fileExtension: String) =
    File(folder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + fileExtension)


fun getOutputDirectory(context: Context): File {
  val appContext = context.applicationContext
  val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
    File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
  }
  return if (mediaDir != null && mediaDir.exists())
    mediaDir
  else
    appContext.filesDir
}
