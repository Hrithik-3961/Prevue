package com.hrithik.prevue

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val name: String,
    val uri: Uri,
    val path: String,
    var bitmap: Bitmap?
) : Parcelable