package com.google.mlkit.vision.camerasample.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.camerasample.util.Event

class CameraViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel(){

    private val _keyDownVolumeEvent=MutableLiveData<Event<Boolean>>()

    val keyDownVolumeEvent:LiveData<Event<Boolean>> get() = _keyDownVolumeEvent

    fun setKeyDownVolumeEvent(){
        _keyDownVolumeEvent.value=Event.dataEvent(true)
    }
}