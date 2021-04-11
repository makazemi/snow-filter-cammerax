package com.google.mlkit.vision.camerasample.ui.fragments

import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.mlkit.vision.camerasample.repository.FileRepository
import com.google.mlkit.vision.camerasample.util.AbsentLiveData
import com.google.mlkit.vision.camerasample.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class ResultPhotoViewModel @ViewModelInject constructor(
    private val repository: FileRepository,
) : ViewModel() {


    private val _resultBitmap = MutableLiveData<Bitmap?>()

    private val _countSaveImageClick = MutableLiveData<Int>()

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


    val saveImageResult: LiveData<DataState<String>> = _resultBitmap.switchMap {
        if (it == null) {
            Timber.d("it==null")
            AbsentLiveData.create()
        } else if (_countSaveImageClick.value ?: 0 > 0) {
            liveData {
                emit(DataState.data("repeat"))

            }
        } else
            repository.saveImage(it).flowOn(Dispatchers.IO).asLiveData()
    }
}