package com.hrithik.prevue

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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

enum class UploadType {
    NONE, GALLERY, CAMERA
}

class HomeViewModel : ViewModel() {

    val image = MutableLiveData<Image>()
    val permissionRequest = MutableLiveData<List<String>>()

    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvents = homeEventChannel.receiveAsFlow()

    private var uploadType = UploadType.NONE

    fun onUploadFromGalleryClicked(activity: FragmentActivity) = viewModelScope.launch {
        uploadType = UploadType.GALLERY
        checkPermissions(activity)
    }

    fun onTakeSelfieClicked(activity: FragmentActivity) = viewModelScope.launch {
        uploadType = UploadType.CAMERA
        checkPermissions(activity)
    }

    fun onImagePicked(uri: Uri, activity: FragmentActivity) = viewModelScope.launch {
        image.value!!.bitmap = getBitmap(uri, activity)
        homeEventChannel.send(HomeEvent.NavigateToEditScreen(image.value!!))
    }

    private fun getBitmap(uri: Uri, activity: FragmentActivity) : Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
        }
    }

    fun onImageCaptured() = viewModelScope.launch {
        val bitmap: Bitmap = BitmapFactory.decodeFile(image.value?.path!!)
        image.value!!.bitmap = bitmap
        homeEventChannel.send(HomeEvent.NavigateToEditScreen(image.value!!))
    }

    private fun checkPermissions(activity: FragmentActivity) {
        val permissionsList = LinkedList<String>()
        permissionsList.add(Manifest.permission.CAMERA)
        permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val requestPermissionsList = LinkedList<String>()
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
                    requestPermissionsList.add(permission)
                }
            }
        }
        if (requestPermissionsList.isNotEmpty())
            permissionRequest.value = requestPermissionsList
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
        val fname = "Image-${System.currentTimeMillis()}.jpg"
        //val file = File(activity.cacheDir, fname)
        val root = File(activity.cacheDir.toString())
        if (!root.exists())
            root.mkdir()
        val file = File(root, fname)
        val uri = FileProvider.getUriForFile(
            activity,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        image.value = Image(fname, uri, file.path, null)

        var intent = Intent()
        if (uploadType == UploadType.GALLERY) {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            homeEventChannel.send(HomeEvent.OpenGallery(intent))
        } else if (uploadType == UploadType.CAMERA) {
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            homeEventChannel.send(HomeEvent.OpenCamera(intent))
        }
    }

    fun onPermissionResult(activity: FragmentActivity, granted: Boolean) {
        if (granted)
            createTempFile(activity)
    }

    sealed class HomeEvent {
        data class OpenGallery(val intent: Intent) : HomeEvent()
        data class OpenCamera(val intent: Intent) : HomeEvent()
        data class NavigateToEditScreen(val image: Image) : HomeEvent()
    }

}