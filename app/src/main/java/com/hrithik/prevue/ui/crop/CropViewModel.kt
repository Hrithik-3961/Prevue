package com.hrithik.prevue.ui.crop

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrithik.prevue.util.Response
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
        data object GetCroppedImage : CropEvent()
        data class NavigateBackWithResult(val bitmap: Bitmap?) : CropEvent()
    }

}