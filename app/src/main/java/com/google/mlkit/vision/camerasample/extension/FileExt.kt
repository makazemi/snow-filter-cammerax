package com.google.mlkit.vision.camerasample.extension

import androidx.fragment.app.Fragment
import com.google.mlkit.vision.camerasample.R
import java.io.File


fun Fragment.getOutputDirectory(): File {
    val appContext = requireContext().applicationContext
    val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}