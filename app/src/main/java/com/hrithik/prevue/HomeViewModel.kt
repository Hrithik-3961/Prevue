package com.hrithik.prevue

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class HomeViewModel : ViewModel() {

    val image = MutableLiveData<Image>()
    val permissionRequest = MutableLiveData<List<String>>()

    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvents = homeEventChannel.receiveAsFlow()

    fun onUploadFromGalleryClicked() = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.NavigateToEditScreen(image.value!!))
    }

    fun onTakeSelfieClicked(activity: FragmentActivity) = viewModelScope.launch {
        val permissions = LinkedList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkPermissions(activity, permissions)
    }

    fun onImageCaptured() = viewModelScope.launch {
        val bitmap: Bitmap = BitmapFactory.decodeFile(image.value?.path!!)
        image.value!!.bitmap = bitmap
        homeEventChannel.send(HomeEvent.NavigateToEditScreen(image.value!!))
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
            onPermissionResult(activity, true)
    }

    private fun createTempFile(activity: FragmentActivity) = viewModelScope.launch {
        /*val file = File(activity.filesDir, "Selfie-${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file)*/
        /* val root = Environment.getRootDirectory()
         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
             .toString()
         val myFile = File("$root/Prevue")
         myFile.mkdirs()*/
        val fname = "Selfie-${System.currentTimeMillis()}.jpg"
        //val file = File(activity.cacheDir, fname)
        val root = File(activity.cacheDir.toString())
        if(!root.exists())
            root.mkdir()
        val file = File(root, fname)
        val uri = FileProvider.getUriForFile(
            activity,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        image.value = Image(fname, uri, file.path, null)
        homeEventChannel.send(HomeEvent.OpenCamera(intent))
    }

    fun onPermissionResult(activity: FragmentActivity, granted: Boolean) = viewModelScope.launch {
        if (granted)
            createTempFile(activity)
    }

    sealed class HomeEvent {
        data class OpenCamera(val intent: Intent) : HomeEvent()
        data class NavigateToEditScreen(val image: Image) : HomeEvent()
    }

}