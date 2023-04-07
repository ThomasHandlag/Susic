package com.example.susic.data

import android.os.Parcel
import android.os.Parcelable


data class PostData(
    val userName: String = "Thomas",
    val ownerId: String = "",
    val id: String = "",
    val urlVisual: String = "",
    val textTitle: String = "",
    val datePost: String = "",
    val imgThumb: String? = "",
    val userImageUrl: String = ""
)