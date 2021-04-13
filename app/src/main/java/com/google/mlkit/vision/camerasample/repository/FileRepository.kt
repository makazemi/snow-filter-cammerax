package com.google.mlkit.vision.camerasample.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.camerasample.R
import com.google.mlkit.vision.camerasample.util.DataState
import com.google.mlkit.vision.camerasample.util.ErrorBody
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@Singleton
class FileRepository (private val context: Context) {

    companion object{
        const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val PREFIX_FILE_CASH="temporary"
        const val PHOTO_EXTENSION = ".jpg"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getUriFromBitmap(bitmap: Bitmap): Flow<DataState<Uri>> = flow {
        Timber.d("getUriFromBitmap")
        emit(DataState.loading(true))
        var uri: Uri?=null
        val outputDir = context.cacheDir // context being the Activity pointer
        val outputFile = File.createTempFile(PREFIX_FILE_CASH, PHOTO_EXTENSION, outputDir)
        try {
            val outputStream = FileOutputStream(outputFile)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                uri = Uri.fromFile(outputFile)
                emit(
                    DataState.data(uri)
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emit(DataState.error<Uri>(message = ErrorBody(message = "fail")))
        }

    }.flowOn(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getCompressFileFromBitmap(bitmap: Bitmap): Flow<DataState<File>> = flow {
        Timber.d("getUriFromBitmap")
        emit(DataState.loading(true))

        val outputDir = context.cacheDir // context being the Activity pointer

        val outputFile = File.createTempFile(FILENAME,PHOTO_EXTENSION, outputDir)
        try {
            val outputStream = FileOutputStream(outputFile)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                val compressedImageFile = Compressor.compress(context, outputFile)
                emit(
                    DataState.data(compressedImageFile)
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emit(DataState.error<File>(message = ErrorBody(message = "fail")))
        }

    }.flowOn(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun saveImage(bitmap: Bitmap): Flow<DataState<String>> = flow {

        emit(
            DataState.loading(isLoading = true)
        )
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/${context.getString(R.string.app_name)}"
            )
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? =
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            Timber.d("if uri=$uri")
            if (uri != null) {
                emit(
                    saveImageToStreamWithFlow(
                        bitmap,
                        context.contentResolver.openOutputStream(uri)
                    )
                )

                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)


            }
        } else {
            val file = getOutputDirectory().createFile(
                FILENAME,
                PHOTO_EXTENSION
            )

            val uri = Uri.fromFile(file)
            Timber.d("else =$uri")
            emit(
                saveImageToStreamWithFlow(bitmap, FileOutputStream(file))
            )
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }

    }.flowOn(Dispatchers.IO)

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        return values
    }

    private fun saveImageToStreamWithFlow(
        bitmap: Bitmap,
        outputStream: OutputStream?
    ): DataState<String> {
        if (outputStream != null) {
            return try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()

                DataState.data("success")

            } catch (e: Exception) {
                e.printStackTrace()
                DataState.error<String>(message = ErrorBody(message = "fail"))
            }
        } else {

            return DataState.error<String>(message = ErrorBody(message = "fail"))

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getOutputDirectory(): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    private fun File.createFile(format: String, extension: String) =
        File(
            this, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )
}