package com.example.susic.data

import java.util.*

data class User(
    var id: String = "",
    var firstname: String = "",
    var lastname: String = "",
    var gender: Int = 0,
    var urlImg: String = "",
    var followers: Int = 0,
    var birthday: Date? = null,
    var contact: String = "",
    var isArtist: Boolean = false,
    var numFr: Int = 0,
    var numPost: Int = 0,
    var numSong: Int = 0,
    var des: String = ""
)

data class Artist(
    val id: String = "",
    val albumToken: String = "",
    val trackNum: Int = 0
)

data class Track(
    val id: String = "",
    val name: String = "",
    val url: String = "",
)

data class Album(
    val id: String = "",
    val tracks: List<String> = listOf(""),
)

data class Comments(
    val id: String = "",
    val content: String = "",
    val dateRep: Date,
    val tag: String = "",
    val user: String = "",
    val url: String = "",
    val like: String = "",
    val liked: Boolean = false,
    val repCount: Int = 0
)