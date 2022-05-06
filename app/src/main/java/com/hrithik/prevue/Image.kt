package com.hrithik.prevue

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Image(
    val name: String,
    val uri: Uri,
) : Parcelable