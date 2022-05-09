package com.hrithik.prevue

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CropViewModel : ViewModel() {
    val image = MutableLiveData<Image>()

    private val cropEventChannel = Channel<CropEvent>()
    val cropEvents = cropEventChannel.receiveAsFlow()

    fun onCancelClicked() {
    }

    fun onDoneClicked() = viewModelScope.launch {
        cropEventChannel.send(CropEvent.GetCroppedImage)
        cropEventChannel.send(CropEvent.NavigateBackWithResult(image.value!!))
    }

    sealed class CropEvent {
        object GetCroppedImage : CropEvent()
        data class NavigateBackWithResult(val image: Image) : CropEvent()
    }

}