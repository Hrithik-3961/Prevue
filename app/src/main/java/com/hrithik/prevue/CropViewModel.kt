package com.hrithik.prevue

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CropViewModel : ViewModel() {
    val bitmap = MutableLiveData<Bitmap>()

    private val cropEventChannel = Channel<CropEvent>()
    val cropEvents = cropEventChannel.receiveAsFlow()

    fun onCancelClicked() {
        TODO()
    }

    fun onDoneClicked() = viewModelScope.launch {
        cropEventChannel.send(CropEvent.GetCroppedImage)
    }

    fun onImageCropped() = viewModelScope.launch {
        cropEventChannel.send(CropEvent.NavigateBackWithResult(bitmap.value!!))
    }

    sealed class CropEvent {
        object GetCroppedImage : CropEvent()
        data class NavigateBackWithResult(val bitmap: Bitmap) : CropEvent()
    }

}