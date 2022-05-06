package com.hrithik.prevue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvents = homeEventChannel.receiveAsFlow()

    fun onUploadFromGalleryClicked() = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.NavigateToEditScreen)
    }

    fun onTakeSelfieClicked() = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.NavigateToEditScreen)
    }

    sealed class HomeEvent {
        object NavigateToEditScreen : HomeEvent()
    }

}