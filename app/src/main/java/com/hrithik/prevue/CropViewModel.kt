package com.hrithik.prevue

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CropViewModel : ViewModel() {
    val bitmap = MutableLiveData<Response<Bitmap>>()

    private val cropEventChannel = Channel<CropEvent>()
    val cropEvents = cropEventChannel.receiveAsFlow()

    fun onCancelClicked() =viewModelScope.launch {
        cropEventChannel.send(CropEvent.NavigateBackWithResult(null))
    }

    fun onDoneClicked() = viewModelScope.launch {
        cropEventChannel.send(CropEvent.GetCroppedImage)
    }

    fun onImageCropped() = viewModelScope.launch {
        val bmp = bitmap.value?.data!!
        cropEventChannel.send(CropEvent.NavigateBackWithResult(bmp))
    }

    sealed class CropEvent {
        object GetCroppedImage : CropEvent()
        data class NavigateBackWithResult(val bitmap: Bitmap?) : CropEvent()
    }

}