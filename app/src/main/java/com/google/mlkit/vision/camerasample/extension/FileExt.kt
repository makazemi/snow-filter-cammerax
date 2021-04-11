package com.google.mlkit.vision.camerasample.extension

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.ui.fragments.CameraFragment
import com.google.mlkit.vision.camerasample.util.DataState
import com.google.mlkit.vision.camerasample.util.ErrorBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Activity.saveImage(bitmap: Bitmap, folderName: String) {
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            saveImageToStream(bitmap,contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            contentResolver.update(uri, values, null, null)
        }
    } else {
        val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + folderName)
        // getExternalStorageDirectory is deprecated in API 29

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = System.currentTimeMillis().toString() + ".png"
        val file = File(directory, fileName)
        saveImageToStream(bitmap, FileOutputStream(file))
        if (file.absolutePath != null) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}

fun Fragment.saveImage(bitmap: Bitmap, folderName: String=getString(R.string.app_name)) {
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            saveImageToStream(bitmap,requireActivity().contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            requireActivity().contentResolver.update(uri, values, null, null)
        }
    } else {
        val file=getOutputDirectory().createFile(
            CameraFragment.FILENAME,
            CameraFragment.PHOTO_EXTENSION)
        saveImageToStream(bitmap, FileOutputStream(file))
        if (file.absolutePath != null) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}

suspend fun Fragment.saveImage(bitmap: Bitmap):Flow<DataState<String>> =flow{
    emit(
        DataState.loading(isLoading = true)
    )
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${getString(R.string.app_name)}")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            emit(
                saveImageToStreamWithFlow(bitmap,requireActivity().contentResolver.openOutputStream(uri))
            )

            values.put(MediaStore.Images.Media.IS_PENDING, false)
            requireActivity().contentResolver.update(uri, values, null, null)
        }
    } else {
        val file=getOutputDirectory().createFile(
            CameraFragment.FILENAME,
            CameraFragment.PHOTO_EXTENSION)
        emit(
            saveImageToStreamWithFlow(bitmap, FileOutputStream(file))
        )
        if (file.absolutePath != null) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }
}

private fun contentValues(): ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    return values
}

private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private suspend fun saveImageToStreamWithFlow(bitmap: Bitmap, outputStream: OutputStream?): DataState<String>{
    if (outputStream != null) {
        return try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            DataState.data("success")

        } catch (e: Exception) {
            e.printStackTrace()

            DataState.error<String>(message = ErrorBody(message = "fail"))

        }
    }else{

        return  DataState.error<String>(message = ErrorBody(message = "fail"))

    }
}
fun Fragment.getOutputDirectory(): File {
    val appContext = requireContext().applicationContext
    val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}
fun File.createFile(format: String, extension: String) =
    File(this, SimpleDateFormat(format, Locale.US)
        .format(System.currentTimeMillis()) + extension)