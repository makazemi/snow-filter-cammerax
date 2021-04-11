package com.google.mlkit.vision.camerasample.ui

import android.graphics.Bitmap
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.mlkit.vision.camerasample.util.Event

class CameraViewModel @ViewModelInject constructor() : ViewModel(){

    private val _keyDownVolumeEvent=MutableLiveData<Event<Boolean>>()

    val keyDownVolumeEvent:LiveData<Event<Boolean>> get() = _keyDownVolumeEvent

    fun setKeyDownVolumeEvent(){
        _keyDownVolumeEvent.value=Event.dataEvent(true)
    }

    private val _resultBitmap=MutableLiveData<Event<Bitmap>>()

    val resultBitmap:LiveData<Event<Bitmap>> get() = _resultBitmap

    fun setResultBitmap(value:Bitmap){
        _resultBitmap.value=Event.dataEvent(value)
    }

}