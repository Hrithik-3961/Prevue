package com.hrithik.prevue

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel : ViewModel() {

    val permissionRequest = MutableLiveData<List<String>>()

    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvents = homeEventChannel.receiveAsFlow()

    fun onUploadFromGalleryClicked() = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.NavigateToEditScreen)
    }

    fun onTakeSelfieClicked(activity: FragmentActivity) = viewModelScope.launch {
        val permissions = LinkedList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermissions(activity, permissions)
    }

    private fun checkPermissions(activity: FragmentActivity, permissionsList: List<String>) {
        val permissions = LinkedList<String>()
        permissionsList.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permission
                    )
                ) {
                    permissions.add(permission)
                }
            }
        }
        if (permissions.isNotEmpty())
            permissionRequest.value = permissions
        else
            onPermissionResult(true)
    }

    fun onPermissionResult(granted: Boolean) = viewModelScope.launch {
        if (granted)
            homeEventChannel.send(HomeEvent.OpenCamera)
    }

    sealed class HomeEvent {
        object OpenCamera : HomeEvent()
        object NavigateToEditScreen : HomeEvent()
    }

}