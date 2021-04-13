package com.google.mlkit.vision.camerasample.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.mlkit.vision.camerasample.repository.FileRepository
import com.google.mlkit.vision.camerasample.util.AbsentLiveData
import com.google.mlkit.vision.camerasample.util.DataState
import com.google.mlkit.vision.camerasample.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ResultPhotoViewModel @Inject constructor(
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _resultBitmap = MutableLiveData<Bitmap?>()

    private val _countSaveImageClick = MutableLiveData<Int>()


    private val _shouldGoToAddDiary=MutableLiveData<Event<Uri>>()


    init {
        _countSaveImageClick.value = 0
    }


    private fun increaseSaveImageClick() {
        _countSaveImageClick.value?.let {
            _countSaveImageClick.value = it + 1
        }
    }

    fun setResultBitmap(value: Bitmap) {
        _resultBitmap.value = value
        increaseSaveImageClick()

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val saveImageResult: LiveData<DataState<String>> = _resultBitmap.switchMap {
        if (it == null) {
            Timber.d("it==null")
            AbsentLiveData.create()
        } else if (_countSaveImageClick.value ?: 0 > 0) {
            liveData {
                emit(DataState.data("repeat"))

            }
        } else
            fileRepository.saveImage(it).asLiveData()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getUriFromBitmap(bitmap: Bitmap)=fileRepository.getUriFromBitmap(bitmap).asLiveData()
}