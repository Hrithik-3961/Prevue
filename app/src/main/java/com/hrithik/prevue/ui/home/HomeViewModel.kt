package com.hrithik.prevue.ui.home

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
import com.hrithik.prevue.BuildConfig
import com.hrithik.prevue.data.Image
import com.hrithik.prevue.util.Response
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class UploadType {
    NONE, GALLERY, CAMERA
}

class HomeViewModel : ViewModel() {

    val image = MutableLiveData<Response<Image?>>()
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
        val img = image.value?.data
        if (img != null) {
            img.bitmap = getBitmap(uri, activity)
            homeEventChannel.send(HomeEvent.NavigateToEditScreen(img))
        }
    }

    fun onImageCaptured() = viewModelScope.launch {
        val img = image.value?.data
        if (img != null) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(img.path)
            img.bitmap = bitmap
            homeEventChannel.send(HomeEvent.NavigateToEditScreen(img))
        }
    }

    fun onPermissionResult(activity: FragmentActivity, granted: Boolean) {
        if (granted) {
            val uri = createTempFile(activity)
            if (uri != null) {
                openGalleryOrCamera(uri)
            } else {
                image.value = Response.error("Error occurred in creating the file")
            }
        } else {
            image.value = Response.error("Storage and camera permissions required to proceed!")
        }
    }

    private fun getBitmap(uri: Uri, activity: FragmentActivity): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
        }
    }

    private fun checkPermissions(activity: FragmentActivity) {
        val permissionsList = LinkedList<String>()
        permissionsList.add(Manifest.permission.CAMERA)
        permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        var flag = true
        val requestList = LinkedList<String>()

        permissionsList.forEach { permission ->
            requestList.add(permission)
            val permissionStatus = ContextCompat.checkSelfPermission(activity, permission)
            if (permissionStatus == PackageManager.PERMISSION_DENIED) {
                flag = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            } else if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                requestList.remove(permission)
            }
        }

        if (requestList.isNotEmpty())
            permissionRequest.value = requestList
        else
            onPermissionResult(activity, flag)
    }

    private fun createTempFile(activity: FragmentActivity): Uri? {
        val sdf = SimpleDateFormat("dd-MM-yy hh_mm_ss a", Locale.ENGLISH)
        val fileName = "Image ${sdf.format(Date())}.jpg"
        try {
            val root = File(activity.cacheDir.toString())
            if (!root.exists())
                root.mkdir()
            val file = File(root, fileName)
            val uri = FileProvider.getUriForFile(
                activity,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            val img = Image(fileName, uri, file.path, null)
            image.value = Response.success(img)
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun openGalleryOrCamera(uri: Uri) = viewModelScope.launch {
        var intent = Intent()
        if (uploadType == UploadType.GALLERY) {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            homeEventChannel.send(HomeEvent.OpenGallery(intent))
        } else if (uploadType == UploadType.CAMERA) {
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            homeEventChannel.send(HomeEvent.OpenCamera(intent))
        }
    }

    sealed class HomeEvent {
        data class OpenGallery(val intent: Intent) : HomeEvent()
        data class OpenCamera(val intent: Intent) : HomeEvent()
        data class NavigateToEditScreen(val image: Image) : HomeEvent()
    }

}