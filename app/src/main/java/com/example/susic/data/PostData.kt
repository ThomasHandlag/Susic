package com.example.susic.data

import java.util.*

data class Post(
    var userName: String = "Thomas",
    var ownerId: String = "",
    var id: String = "",
    var urlVisual: String = "",
    var textTitle: String = "",
    var datePost: Date = Date(),
    var imgThumb: String = "",
    var userImageUrl: String = ""
)

data class PostData(
    val id: String = "",
    val urlVisual: String = "",
    val textTitle: String = "",
    val datePost: Date,
    val imgThumb: String = "",
)