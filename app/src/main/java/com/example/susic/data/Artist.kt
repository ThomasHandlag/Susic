package com.example.susic.data

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val urlImg: String = "",
    val followers: Int = 0,
    val contact : String = "",
    )

data class Artist(
    val id: String = "",
    val albumToken: String = "",
    val trackNum : Int = 0
)

data class Track(
    val id: String = "",
    val name: String = "",
    val uri: String = "",
    )

data class Album(
    val id: String = "",
    val tracks: List<String> = listOf(""),
)
//import com.squareup.moshi.Json
//data class Artist(
//    @Json(name = "id") val id: String = "",
//    @Json(name = "name") val name: String = "",
//    @Json(name = "uri") val uri: String = "",
//    @Json(name = "images") val lists: List<ImageURI> = listOf(),
//    @Json(name="followers") val followers : Int = 0
//)
//
//data class Song(
//    @Json(name = "id") val id: String = "",
//    @Json(name = "name") val name: String = "",
//    @Json(name = "uri") val uri: String = "",
//    @Json(name = "albumOfTrack") val albums: Albums = Albums(artist = Artist(), coverArt = listOf())
//)
//
//data class Albums(
//    @Json(name = "id") val id: String = "",
//    @Json(name = "uri") val uri: String = "",
//    @Json(name = "") val tracksOfAlbums: List<Song> = listOf(),
//    @Json(name = "name") val nameAlbum: String = "",
//    @Json(name = "") val artist: Artist,
//    @Json(name = "coverArt") val coverArt: List<CoverArt>,
//    @Json(name = "shareUrl") val shareInfo: String = ""
//)
//
//data class CoverArt(@Json(name = "sources") val item: List<ImageURI>)
//data class ImageURI(
//    @Json(name = "url") val url: String = "",
//    @Json(name = "width") val w: Int = 364,
//    @Json(name = "height") val h: Int = 200
//)
//
//data class Tracks(@Json(name = "items") val listTracks: List<Song>)

//38:87:d5:53:fd:db