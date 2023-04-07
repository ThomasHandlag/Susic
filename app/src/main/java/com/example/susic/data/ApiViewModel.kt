package com.example.susic.data

import android.util.Log
import androidx.lifecycle.*
import com.example.susic.network.DB
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ApiViewModel(private val db: FirebaseFirestore, private val storage: FirebaseStorage) :
    ViewModel() {
    private val _posts = MutableLiveData<List<PostData>>()
    private val lists = mutableListOf(PostData())
    private var storageRef: StorageReference = storage.reference
    private val _artist = MutableLiveData<List<Artist>>()

    init {
        val initPosts = DB.getPosts()
        //make sure the data from fire base is init
        if (!DB.sIsLoadingData) {
            _posts.value = initPosts
        }
    }

    val artist: LiveData<List<Artist>>
        get() = _artist
    val posts: LiveData<List<PostData>>
        get() = _posts
}

//uncomment for this when add dependencies (according to google developer page)
class ApiViewModelFactor(private val db: FirebaseFirestore, private val storage: FirebaseStorage) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApiViewModel::class.java)) {
            return ApiViewModel(db, storage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}