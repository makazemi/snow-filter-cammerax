package com.google.mlkit.vision.camerasample.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.ui.fragments.CameraFragment
import com.google.mlkit.vision.camerasample.util.DataState
import com.google.mlkit.vision.camerasample.util.ErrorBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@Singleton
class FileRepository (private val context: Context) {

     fun saveImage(bitmap: Bitmap): Flow<DataState<String>> = flow{

             emit(
                 DataState.loading(isLoading = true)
             )
             if (Build.VERSION.SDK_INT >= 29) {
                 val values = contentValues()
                 values.put(MediaStore.Images.Media.RELATIVE_PATH,
                     "Pictures/${context.getString(R.string.app_name)}")
                 values.put(MediaStore.Images.Media.IS_PENDING, true)
                 // RELATIVE_PATH and IS_PENDING are introduced in API 29.

                 val uri: Uri? =
                     context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                         values)
                 if (uri != null) {
                     //withContext(Dispatchers.IO){
                         emit(
                             saveImageToStreamWithFlow(bitmap,
                                 context.contentResolver.openOutputStream(uri))
                         )

                         values.put(MediaStore.Images.Media.IS_PENDING, false)
                         context.contentResolver.update(uri, values, null, null)
                    // }

                 }
             } else {
                // withContext(Dispatchers.IO) {
                     val file = getOutputDirectory().createFile(
                         CameraFragment.FILENAME,
                         CameraFragment.PHOTO_EXTENSION)
                     emit(
                         saveImageToStreamWithFlow(bitmap, FileOutputStream(file))
                     )
                     if (file.absolutePath != null) {
                         val values = contentValues()
                         values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                         // .DATA is deprecated in API 29
                         context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                             values)
                     }
                 //}
             }

    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private  fun saveImageToStreamWithFlow(bitmap: Bitmap, outputStream: OutputStream?): DataState<String> {
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
    private fun getOutputDirectory(): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }
    private fun File.createFile(format: String, extension: String) =
        File(this, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension)
}