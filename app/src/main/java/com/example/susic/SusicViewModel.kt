 package com.example.susic

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.susic.data.Album
import com.example.susic.data.Artist
import com.example.susic.data.Comments
import com.example.susic.data.Post
import com.example.susic.data.Track
import com.example.susic.data.User
import com.example.susic.network.DB
import com.example.susic.network.LOG_TAG
//import com.example.susic.network.SpotifyMusic
import com.example.susic.network.StateListener
import com.example.susic.ui.notify.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.Objects
import javax.net.ssl.SSLEngineResult.Status

 class SusicViewModel : ViewModel() {

    private var _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>>
        get() = _posts

    private val _loadState = MutableLiveData<StatusEnums>()
    val loadState: LiveData<StatusEnums>
        get() = _loadState

    private val _commentState = MutableLiveData<StatusEnums>()
    val commentState: LiveData<StatusEnums>
        get() = _commentState

    private val _comments = MutableLiveData<List<Comments>>()
    val comments: LiveData<List<Comments>>
        get() = _comments

    private val _sTracks = MutableLiveData<List<Track>>()
    val sTrack: LiveData<List<Track>> get() = _tracks

    private val _replies = MutableLiveData<List<Comments>>()
    val replies: LiveData<List<Comments>>
        get() = _replies

    private val _numOfPost = MutableLiveData<Int>()

    private val _mainComm = MutableLiveData<Comments>()
    val mainComm: LiveData<Comments>
        get() = _mainComm

    private val _sCurrentMediaType = MutableLiveData<SusicConstants>()
    val sCurrentMediaType: LiveData<SusicConstants>
        get() = _sCurrentMediaType

    private val _sPlayingTrack = MutableLiveData<Track>()
    val sPlayingTrack: LiveData<Track>
        get() = _sPlayingTrack

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User>
        get() = _currentUser

    private val _headerState = MutableLiveData<StatusEnums>()
    val headerState: LiveData<StatusEnums>
        get() = _headerState

    private val _notifyState = MutableLiveData<StatusEnums>()
    val notifyState: LiveData<StatusEnums>
        get() = _notifyState

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>>
        get() = _artists

    private val _artistDetail = MutableLiveData<Pair<List<Track>, Album>>()
    val artistDetail: LiveData<Pair<List<Track>, Album>> get() = _artistDetail

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _currentViewUser = MutableLiveData(User())
    val currentViewUser: LiveData<User>
        get() = _currentViewUser

    private val _track = MutableLiveData<Track>()
    val track: LiveData<Track>
        get() = _track

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>>
        get() = _users

    val file = MutableLiveData<Uri>()

    val title = MutableLiveData<String>()

    val audioFile = MutableLiveData<Uri>()

    private val _uploadPost = MutableLiveData<StatusEnums>()
    val uploadPost: LiveData<StatusEnums>
        get() = _uploadPost

    private val _usersLoadState = MutableLiveData<StatusEnums>()
    val usersLoadState: LiveData<StatusEnums>
        get() = _usersLoadState

    private val _notifications = MutableLiveData<List<Notification>>()
    val notification: LiveData<List<Notification>>
        get() = _notifications


    private val _repState = MutableLiveData<StatusEnums>()
    val repState: LiveData<StatusEnums>
        get() = _repState

    val postId = MutableLiveData<String>()

    val repName = MutableLiveData("")

    val repId = MutableLiveData("")

    private val _tracksLoadState = MutableLiveData<StatusEnums>()
    val trackLoadState: LiveData<StatusEnums>
        get() = _tracksLoadState

    private val _artistLoadState = MutableLiveData<StatusEnums>()
    val artistLoadState: LiveData<StatusEnums>
        get() = _artistLoadState

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>>
        get() = _tracks

    val _updateUserState = MutableLiveData<Boolean>()

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
        override fun onUpdateUserSuccess(rs: Boolean) {
            _updateUserState.value = rs
        }
        override fun onGetUsersPosts(rs: StatusEnums) {
            listProfState.value = rs
        }
        override fun onSTrackState(rs: StatusEnums) {
            sTrackState.value = rs
        }
    }
    private val _searchKey = MutableLiveData("")
    private val _type = MutableLiveData(0)
    fun setSearchKey(key: String) {
        _searchKey.value = key
    }
    fun setSearchType(type: Int) {
        _type.value= type
    }
    val sTrackState = MutableLiveData<StatusEnums>()
    fun search(key: String) {
        _sTracks.value = DB.searchTrack(key)
//        _type.value?.let { SpotifyMusic.search(it, key,10) }
    }
    val listProfState = MutableLiveData<StatusEnums>()
    val listProf = MutableLiveData<List<Post>>()
    fun aGetPosts() {
        Log.e(LOG_TAG, DB.getPosts(10).size.toString() + " length")
        _posts.value = DB.getPosts(10)
    }

    fun getSpecificPost(id: String) {
        listProf.value = DB.getSpecificPost(id)
    }
    fun getAmountOfPosts() {

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
        postId.value?.let {
            DB.onListenCommentState(it)
            if (type == 0) DB.comment(str, it, type)
            else repId.value?.let { it1 -> DB.reply(it, str, it1) }
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

    init {
        val callBackState = object : AppCallBackState {
            override fun onStateChange(state: StatusEnums) {
                _headerState.value = state
            }

            override fun onPostUploadSuccess(status: StatusEnums) {
                _uploadPost.value = status
            }

            override fun onListenLoadState(status: StatusEnums) {
                _usersLoadState.value = status
            }

            override fun getFriendNum(n: Int) {
                _currentViewUser.value?.numFr = n
            }

            override fun getPostsNum(n: Int) {
                _currentViewUser.value?.numPost = n
            }

            override fun getSongsNum(n: Int) {
                _currentViewUser.value?.numSong = n
            }

            override fun listenNotification() {
                getUserNotification()
            }

            override fun onListNotifyState(st: StatusEnums) {
                _notifyState.value = st
            }

            override fun onLoadListSongs(st: StatusEnums) {
                _tracksLoadState.value = st
            }

            override fun onLoadListArtists(st: StatusEnums) {
                _artistLoadState.value = st
            }

        }
        DB.state = stateListener
        DB.callBackState = callBackState
        aGetPosts()
    }

    fun iGetCurrentUser() {
        _currentUser.value = DB.getCurrentUser()
    }

    fun getUserNotification() {
        _notifications.value = DB.getNotifications(Firebase.auth.uid.toString())
    }

    fun getArtists() {
        _artists.value = DB.getArtist()
    }

    fun getTracks() {
        _tracks.value = DB.getTracks()
    }

    fun getViewUsers() {
        _users.value = DB.getUsers()
    }

    fun searchTracksByGenre(type: Int) {

    }

    fun getArtistInfo(id: String) {
        _artistDetail.value = DB.getArtistDetail(id)
    }

    fun getAlbumsByArtist(id: String) {

    }

    fun getSpecificAlbums(id: String) {

    }

    fun request(id: String) {
        DB.request(id)
    }

    fun setCurrentViewedUser(user: User) {
        _currentViewUser.value = user
        DB.countNum(user.id)
    }

    fun setPost() {
        if (title.value != null && file.value != null && audioFile.value != null) {
            DB.writePost(title.value!!, file.value, audioFile.value)
        }
    }

    fun updateUserInfo(f: String="", l:String="", cont: String="", gen: Int =0){
        DB.updateUserInfo(f, l, cont, gen, file.value)
    }
    fun accept(id: String) {
        DB.acceptFriend(id)
    }

    fun reject(id: String) {
        DB.reject(id)
    }
}

interface AppCallBackState {
    fun onStateChange(state: StatusEnums)
    fun onPostUploadSuccess(status: StatusEnums)
    fun onListenLoadState(status: StatusEnums)
    fun getFriendNum(n: Int)
    fun getPostsNum(n: Int)
    fun getSongsNum(n: Int)
    fun listenNotification()
    fun onListNotifyState(st: StatusEnums)
    fun onLoadListSongs(st: StatusEnums)
    fun onLoadListArtists(st: StatusEnums)
}
