package com.hrithik.prevue

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.view.animation.RotateAnimation
import androidx.collection.LruCache
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.SoftReference


class EditViewModel : ViewModel() {
    val image = MutableLiveData<Image>()
    private var mRotation = 0F

    private val cacheSize = 8 * 1024 * 1024
    private val cache = LruCache<Int, SoftReference<Bitmap>>(cacheSize)

    private val editEventChannel = Channel<EditEvent>()
    val editEvents = editEventChannel.receiveAsFlow()

    fun onCancelClicked() {
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
            TODO("Show error message")
    }

    private fun saveImage(activity: FragmentActivity): File? {
        var file: File? = null
        try {
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
            val myFile = File("$root/Prevue")
            myFile.mkdirs()
            val fileName = image.value?.name!!
            file = File(myFile, fileName)
            file.createNewFile()
            val bitmap = image.value?.bitmap!!

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()

            MediaScannerConnection.scanFile(activity, arrayOf(file.toString()), null) { _, _ -> }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    fun onUndoClicked() = viewModelScope.launch {
        val data = cache.remove(cache.size() - 1)?.get()!!
        val img = image.value!!
        img.bitmap = data
        image.value = img
    }

    fun onCropClicked() = viewModelScope.launch {
        val bitmap = image.value!!.bitmap!!
        cache.put(cache.size(), SoftReference(bitmap))
        editEventChannel.send(EditEvent.NavigateToCropScreen(bitmap))
    }

    fun onRotateLeftClicked() = viewModelScope.launch {
        cache.put(cache.size(), SoftReference(image.value?.bitmap))
        mRotation -= 90
        editEventChannel.send(EditEvent.Rotate(getRotateAnimation(mRotation + 90)))
        val matrix = Matrix()
        var bitmap = image.value?.bitmap!!
        matrix.postRotate(-90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        image.value?.bitmap = bitmap
    }

    fun onRotateRightClicked() = viewModelScope.launch {
        cache.put(cache.size(), SoftReference(image.value?.bitmap))
        mRotation += 90
        editEventChannel.send(EditEvent.Rotate(getRotateAnimation(mRotation - 90)))
        val matrix = Matrix()
        var bitmap = image.value?.bitmap!!
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        image.value?.bitmap = bitmap
    }

    private fun getRotateAnimation(fromDegrees: Float): RotateAnimation {
        val rotateAnimation = RotateAnimation(
            fromDegrees, mRotation, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.fillAfter = true
        return rotateAnimation
    }

    sealed class EditEvent {
        data class NavigateToCropScreen(val bitmap: Bitmap) : EditEvent()
        data class NavigateBackWithResult(val image: Image) : EditEvent()
        data class Rotate(val rotateAnimation: RotateAnimation) : EditEvent()
    }

}