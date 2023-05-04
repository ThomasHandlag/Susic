package com.example.susic.data

import android.util.Log
import androidx.lifecycle.*
import com.example.susic.StatusEnums
import com.example.susic.network.DB
import com.example.susic.network.StateListener
import kotlinx.coroutines.launch

class PlayerViewModel() :
    ViewModel() {
    private var _posts = MutableLiveData<List<Post>>()
    private var _loadState = MutableLiveData<StatusEnums>()
    private var _commentState = MutableLiveData<StatusEnums>()
    private var _comments = MutableLiveData<List<Comments>>()
    private var _replies = MutableLiveData<List<Comments>>()
    private var _numOfPost = MutableLiveData<Int>()
    private var _mainComm = MutableLiveData<Comments>()

    val mainComm: LiveData<Comments>
        get() = _mainComm

    val replies: LiveData<List<Comments>>
        get() = _replies

    private val _repState = MutableLiveData<StatusEnums>()
    val repState: LiveData<StatusEnums>
        get() = _repState

    val postId = MutableLiveData<String>()

    val repName = MutableLiveData("")

    val repId = MutableLiveData("")

    val comments: LiveData<List<Comments>>
        get() = _comments

    val commentState: LiveData<StatusEnums>
        get() = _commentState

    val loadState: LiveData<StatusEnums>
        get() = _loadState

    val posts: LiveData<List<Post>>
        get() = _posts

    private val stateListener = object : StateListener {
        override fun onSuccess(rs: StatusEnums) {
            _loadState.value = rs
        }

        override fun onCommentSuccess(rs: StatusEnums) {
            _commentState.value = rs
        }

        override fun onListenCommentStateChange() {
            repId.value = ""
        }

        override fun onListenCommentChange() {
            postId.value?.let { getComments(it) }
        }

        override fun onListenReplySuccess(rs: StatusEnums) {
            _repState.value = rs
        }
    }

    init {
        DB.state = stateListener
        _posts.value = DB.getPosts(10)
    }

    fun getSpecificPosts() {
        _posts.value = DB.getPosts(10)
    }

    fun getComments(id: String) {
        _comments.value = DB.getComments(id)
    }

    //destroy the snapshot listener of the post
    fun destroyCommentListener() {
        DB.onDestroyListenComment()
        _comments.value = listOf()
    }

    fun like(id: String, like: String) {
        DB.like(id, like.toInt())
    }

    fun setComment(str: String, type: Int) {
        viewModelScope.launch {
            postId.value?.let {
                DB.onListenCommentState(it)
                if (type == 0) DB.comment(str, it, type)
                else repId.value?.let { it1 -> DB.reply(it, str, it1) }
            }
        }
    }

    fun getReplies(id: String) {
        viewModelScope.launch {
           _replies.value = DB.getReplies(id)
        }
    }

    fun mainComments(it: Comments) {
        _mainComm.value = it
    }
}