package com.example.susic

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.susic.data.Artist
import com.example.susic.data.Track
import com.example.susic.data.User
import com.example.susic.network.DB
import com.example.susic.ui.notify.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SusicViewModel : ViewModel() {

    private var _sCurrentMediaType = MutableLiveData<SusicConstances>()
    val sCurrentMediaType: LiveData<SusicConstances>
        get() = _sCurrentMediaType

    private var _sPlayingTrack = MutableLiveData<Track>()
    val sPlayingTrack: LiveData<Track>
        get() = _sPlayingTrack

    private var _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User>
        get() = _currentUser

    private var _headerState = MutableLiveData<StatusEnums>()
    val headerState: LiveData<StatusEnums>
        get() = _headerState

    private var _comments = MutableLiveData<List<Notification>>()
    val comments: LiveData<List<Notification>>
        get() = _comments

    private var _notifyState = MutableLiveData<StatusEnums>()
    val notifyState: LiveData<StatusEnums>
        get() = _notifyState

    private val _artist = MutableLiveData<List<Artist>>()
    val artist: LiveData<List<Artist>>
        get() = _artist

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

        }
        DB.callBackState = callBackState
//        iGetCurrentUser()
    }
    fun iGetCurrentUser() {
        _currentUser.value = DB.getCurrentUser()
    }
    fun getUserNotification() {
        _notifications.value = DB.getNotifications(Firebase.auth.uid.toString())
    }

    fun getArtists() {

    }

    fun getViewUsers() {
        _users.value = DB.getUsers()
    }

    fun searchTracksByGenre(type: Int) {

    }

    fun getArtistInfo(id: String) {

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
}
