package com.hrithik.prevue

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class EditViewModel : ViewModel() {
    val image = MutableLiveData<Image>()

    private val editEventChannel = Channel<EditEvent>()
    val editEvents = editEventChannel.receiveAsFlow()

    fun onCancelClicked() {
    }

    fun onSaveClicked() {

    }

    fun onUndoClicked() {

    }

    fun onCropClicked() = viewModelScope.launch {
        editEventChannel.send(EditEvent.NavigateToCropScreen(image.value!!))
    }

    fun onRotateLeftClicked() {
        val matrix = Matrix()
        var bitmap = image.value?.bitmap!!
        matrix.postRotate(-90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val img = image.value
        img?.bitmap = bitmap
        image.value = img!!
    }

    fun onRotateRightClicked() {
        val matrix = Matrix()
        var bitmap = image.value?.bitmap!!
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        val img = image.value
        img?.bitmap = bitmap
        image.value = img!!
    }

    sealed class EditEvent {
        data class NavigateToCropScreen(val image: Image) : EditEvent()
    }

}