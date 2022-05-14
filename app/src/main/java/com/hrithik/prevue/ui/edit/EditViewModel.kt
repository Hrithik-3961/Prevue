package com.hrithik.prevue.ui.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.collection.LruCache
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrithik.prevue.data.Image
import com.hrithik.prevue.util.Response
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.SoftReference


class EditViewModel : ViewModel() {
    val image = MutableLiveData<Response<Image>>()
    //private var mRotation = 0F

    private val cacheSize = 8 * 1024 * 1024
    private val cache = LruCache<Int, SoftReference<Bitmap>>(cacheSize)

    private val editEventChannel = Channel<EditEvent>()
    val editEvents = editEventChannel.receiveAsFlow()

    fun onCancelClicked() = viewModelScope.launch {
        editEventChannel.send(EditEvent.NavigateBackWithResult(null))
    }

    fun onSaveClicked(activity: FragmentActivity) = viewModelScope.launch {
        val file = saveImage(activity)
        if (file != null) {
            editEventChannel.send(
                EditEvent.NavigateBackWithResult(
                    Image(
                        file.name,
                        Uri.fromFile(file),
                        file.path,
                        BitmapFactory.decodeFile(file.path),
                    )
                )
            )
        } else
            image.value = Response.error("Unable to save the image!")
    }

    fun onUndoClicked() {
        val data = cache.remove(cache.size() - 1)?.get()
        if (data != null) {
            val img = image.value?.data
            if (img != null) {
                img.bitmap = data
                image.value = Response.success(img)
            }
        }
    }

    fun onCropClicked() = viewModelScope.launch {
        val img = image.value?.data!!
        val bitmap = img.bitmap!!
        cache.put(cache.size(), SoftReference(bitmap))
        editEventChannel.send(EditEvent.NavigateToCropScreen(bitmap))
    }

    fun onRotateLeftClicked() {
        rotateBitmap(-90F)
    }

    fun onRotateRightClicked() {
        rotateBitmap(90F)
    }

    private fun rotateBitmap(degrees: Float) {
        val img = image.value?.data!!
        cache.put(cache.size(), SoftReference(img.bitmap))
        val matrix = Matrix()
        var bitmap = img.bitmap!!
        matrix.postRotate(degrees)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        img.bitmap = bitmap
        image.value = Response.success(img)
    }

    private fun saveImage(activity: FragmentActivity): File? {
        val file: File?
        try {
            val root =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val myDir = File(root, "Prevue")
            if (!myDir.exists()) {
                if (!myDir.mkdirs()) {
                    return null
                }
            }
            val img = image.value?.data!!
            val fileName = img.name
            file = File(myDir.path + File.separator + fileName)
            if (file.exists())
                file.delete()
            val flag = file.createNewFile()
            if (!flag) {
                return null
            }
            val bitmap = img.bitmap!!

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()

            MediaScannerConnection.scanFile(activity, arrayOf(file.toString()), null) { _, _ -> }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    sealed class EditEvent {
        data class NavigateToCropScreen(val bitmap: Bitmap) : EditEvent()
        data class NavigateBackWithResult(val image: Image?) : EditEvent()
    }
}