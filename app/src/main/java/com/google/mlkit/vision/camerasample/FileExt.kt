package com.google.mlkit.vision.camerasample

import android.app.Activity
import android.content.Context
import android.os.Environment
import java.io.File

val rootFolder =
        File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Macgyver${File.separator}"
        ).apply {
            if (!exists())
                mkdirs()
        }

//fun makeTempFile(): File = File.createTempFile("${System.currentTimeMillis()}", ".png", rootFolder)
fun Context.makeTempFile(): File {
    val file = File(getExternalFilesDir(
            Environment.DIRECTORY_PICTURES), getString(R.string.app_name))

    return if (!file.mkdirs())
        file else filesDir
}