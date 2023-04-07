package com.example.susic.network

import androidx.lifecycle.LiveData
import com.example.susic.data.PostData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

interface DBInterface {
    val db: FirebaseFirestore
    fun getPosts(): List<PostData>
}

object DB : DBInterface {
    @JvmStatic
    private var isLoadingData: Boolean = true

    val sIsLoadingData : Boolean
        get() = isLoadingData

    override val db: FirebaseFirestore
        get() = Firebase.firestore

    override fun getPosts(): List<PostData> {
        val lists = mutableListOf<PostData>()
        db.collection("posts").get().addOnSuccessListener { result ->
            if (!result.isEmpty) for (doc in result) {
                lists.add(
                    initPost(doc.data)
                )
            }
            isLoadingData = false
        }.addOnFailureListener {
            isLoadingData = true
        }
        return lists
    }

    private fun initPost(map: Map<String, Any>): PostData =
        PostData(
            userName = map["userName"].toString(),
            ownerId = map["ownerId"].toString(),
            id = map["id"].toString(),
            urlVisual = map["urlVisual"].toString(),
            textTitle = map["textTitle"].toString(),
            datePost = map["datePost"].toString(),
            imgThumb = map["imgThumb"].toString(),
            userImageUrl = map["userImgUrl"].toString()
        )
}
//private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//TODO: Using for connect to api service or custom server
//interface ApiService {
//
//    companion object {
//        private val client = OkHttpClient.Builder().build()
//        operator fun invoke(): ApiService {
//            return Retrofit.Builder()
//                .baseUrl("")
//                .addConverterFactory(MoshiConverterFactory.create(moshi))
//                .client(client).build()
//                .create(ApiService::class.java)
//        }
//    }
//
//    @Headers(
//
//    )
//    @GET("")
//    suspend fun getArtists(): List<Artist>
//
//    @Headers(
//
//    )
//    @GET("")
//    suspend fun getTracks(): List<Song>
//
//    @Headers(
//
//    )
//    @GET("")
//    suspend fun getTrackFromId(@Path("id") id: String): List<Song>
//}
//
//object Api {
//    val retroFitApi = ApiService.invoke()
//}


