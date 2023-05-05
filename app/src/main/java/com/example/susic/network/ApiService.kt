package com.example.susic.network

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import com.example.susic.AppCallBackState
import com.example.susic.StatusEnums
import com.example.susic.data.Album
import com.example.susic.data.Artist
import com.example.susic.data.Comments
import com.example.susic.data.Post
import com.example.susic.data.PostData
import com.example.susic.data.Track
import com.example.susic.data.User
import com.example.susic.ui.notify.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.random.Random

interface StateListener {
    fun onSuccess(rs: StatusEnums)
    fun onCommentSuccess(rs: StatusEnums)
    fun onListenCommentStateChange()
    fun onListenCommentChange()
    fun onListenReplySuccess(rs: StatusEnums)
    fun onUpdateUserSuccess(rs: Boolean)
    fun onGetUsersPosts(rs: StatusEnums)
}

const val LOG_TAG = "AppData"

object DB {
    lateinit var state: StateListener
    lateinit var callBackState: AppCallBackState

    @JvmStatic
    val db: FirebaseFirestore
        get() = Firebase.firestore

    private val listGenre = listOf<Map<String, String>>(
        mapOf(
            "name" to "pop",
            "thumb" to "https://i.pinimg.com/236x/53/33/ac/5333acf5f98a3d47c3c17403a5e1e6c7.jpg",
        ),
        mapOf(
            "name" to "edm",
            "thumb" to "https://i.pinimg.com/236x/95/26/09/952609ee4f098052b69acfaf39b3349d.jpg",
        ),
        mapOf(
            "name" to "r&b",
            "thumb" to "https://i.pinimg.com/236x/53/33/ac/5333acf5f98a3d47c3c17403a5e1e6c7.jpg",
        ),
        mapOf(
            "name" to "country",
            "thumb" to "https://i.pinimg.com/236x/a3/03/02/a30302da6060b6bea4c2c58d36f0dd49.jpg",
        ),
        mapOf(
            "name" to "classical",
            "thumb" to "https://i.pinimg.com/236x/3d/75/0e/3d750ec018b9c476e1989e991aa90178.jpg",
        ),
        mapOf(
            "name" to "rap/hiphop",
            "thumb" to "https://i.pinimg.com/236x/88/f9/15/88f91586d2bd17ef8530c4e7c4bf4b5c.jpg",
        )
    )

    private const val nChars = "abcdefghjklmnouivtxrwqzABCDEFGHIJKLMNOPQSRTUVXYWZ0123456789"
    private val postsRef = db.collection("posts")
    private val userRef = db.collection("users")
    private val albumRef = db.collection("albums")
    private val artistRef = db.collection("artists")
    private val comRef = db.collection("comments")
    private val requestRef = db.collection("requests")
    private val likeRef = db.collection("like")
    private val storeRef = Firebase.storage.reference
    private val frRef = db.collection("friend")
    private val trackRef = db.collection("tracks")
    private var listen: ListenerRegistration? = null
    private var likeListener: ListenerRegistration? = null

    @JvmStatic
    fun onListenCommentState(id: String) {
        listen = comRef.whereGreaterThanOrEqualTo("id", id.substring(5, 10))
            .addSnapshotListener { _, e ->
                if (e != null) {
                    Log.i(LOG_TAG, "listener error", e)
                    return@addSnapshotListener
                }
                state.onListenCommentChange()
            }
//        likeListener = comRef.addSnapshotListener { _, e ->
//            if (e != null) {
//                Log.i(LOG_TAG, "listener error", e)
//                return@addSnapshotListener
//            }
//            state.onListenCommentChange()
//        }
    }

    @JvmStatic
    fun destroyNotificationListener() {
        noListener?.remove()
    }

    @JvmStatic
    fun onDestroyListenComment() {
        listen?.remove()
        likeListener?.remove()
    }

    @JvmStatic
    fun getPosts(n: Long): List<Post> {
        val posts = mutableListOf<Post>()
        val postData = mutableListOf<PostData>()
        state.onSuccess(StatusEnums.LOADING)
        postsRef.orderBy("datePost", Query.Direction.DESCENDING).limit(n).get()
            .addOnSuccessListener { rs ->
                if (!rs.isEmpty) for (doc in rs) {
                    postData.add(initPostData(doc.data))
                }
                for (e in postData) {
                    posts.add(initPost(e))
                }
                if (::state.isInitialized) {
                    state.onSuccess(StatusEnums.DONE)
                }
            }.addOnFailureListener {
                if (::state.isInitialized) {
                    state.onSuccess(StatusEnums.ERROR)
                }
            }
        return posts
    }

    @JvmStatic
    fun reply(id: String, cont: String, repId: String) {
        val i = "${id.substring(5, 10)}${generateId()}"
        comRef.document(i).set(
            hashMapOf(
                "id" to i,
                "tag" to "rep",
                "content" to cont,
                "uid" to repId,
                "like" to 0,
                "date" to Timestamp(Calendar.getInstance().time)
            )
        )
    }

    @JvmStatic
    fun comment(str: String, id: String, type: Int) {
        val i = "${id.substring(5, 10)}${generateId()}"
        val data = hashMapOf(
            "content" to str,
            "id" to i,
            if (type == 0) "tag" to "main"
            else "tag" to "rep",
            "like" to 0,
            "date" to Timestamp(Calendar.getInstance().time),
            "pid" to id,
            "uid" to Firebase.auth.uid.toString()
        )
        comRef.document(i).set(data).addOnSuccessListener {
            state.onListenCommentStateChange()
        }.addOnFailureListener {

        }
    }

    @JvmStatic
    private fun countRep(id: String): Int {
        var i = 0
        comRef.whereGreaterThanOrEqualTo("id", id).get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (e in it) {
                    i += 1
                }
            }
        }
        return i
    }

    @JvmStatic
    fun getReplies(id: String): List<Comments> {
        state.onListenReplySuccess(StatusEnums.LOADING)
        val l = mutableListOf<Comments>()
        comRef.whereEqualTo("uid", id).get().addOnSuccessListener { rs ->
            if (!rs.isEmpty) for (e in rs) {
                var bol = false
                likeRef.whereEqualTo("id", e.data["id"])
                    .whereGreaterThanOrEqualTo("id", Firebase.auth.uid.toString()).get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            bol = true
                        }
                        userRef.whereEqualTo(
                            "id",
                            e.data["oid"].toString()
                        ).get().addOnSuccessListener { it1 ->
                            for (u in it1) {
                                l.add(
                                    Comments(
                                        e.data["id"].toString(),
                                        e.data["content"].toString(),
                                        convertToDate(e.data["date"]),
                                        e.data["tag"].toString(),
                                        u.data["firstname"].toString() + " " + u.data["lastname"].toString(),
                                        u.data["urlImg"].toString(),
                                        e.data["like"].toString(),
                                        bol
                                    )
                                )
                            }
                            state.onListenReplySuccess(StatusEnums.DONE)
                            Log.i(LOG_TAG, l.toString() + "is this null")
                        }.addOnFailureListener { ex ->
                            Log.i(LOG_TAG, ex.stackTraceToString())
                            state.onListenReplySuccess(StatusEnums.ERROR)
                        }
                    }.addOnFailureListener { ex ->
                        Log.i(LOG_TAG, ex.stackTraceToString())
                    }
            }
        }.addOnFailureListener {
            Log.i(LOG_TAG, it.stackTraceToString())
        }
        return l
    }

    @JvmStatic
    fun getCurrentUser(): User {
        val user = User()
        callBackState.onStateChange(StatusEnums.LOADING)
        userRef.whereEqualTo("id", Firebase.auth.uid.toString()).get()
            .addOnSuccessListener { rs ->
                if (!rs.isEmpty) for (e in rs) {
                    with(user) {
                        id = e.data["id"].toString()
                        firstname = e.data["firstname"].toString()
                        lastname = e.data["lastname"].toString()
                        urlImg = e.data["urlImg"].toString()
                        isArtist = e.data["isArtist"] as Boolean
                        birthday = convertToDate(e.data["birthday"])
                        contact = e.data["contact"].toString()
                    }
                }
                callBackState.onStateChange(StatusEnums.DONE)
            }
            .addOnFailureListener {
                callBackState.onStateChange(StatusEnums.ERROR)
            }
        return user
    }


    @JvmStatic
    private fun initPost(postData: PostData): Post {
        val temp = Post()
        var userNameTemp = ""
        var url = ""
        userRef.whereEqualTo("id", postData.uid).get()
            .addOnSuccessListener { rs ->
                for (e in rs) {
                    userNameTemp =
                        e.data["firstname"].toString() + " " + e.data["lastname"].toString()
                    url = e.data["urlImg"].toString()
                }
                with(temp) {
                    id = postData.id
                    imgThumb = postData.imgThumb
                    urlVisual = postData.urlVisual
                    datePost = postData.datePost
                    textTitle = postData.textTitle
                    userName = userNameTemp
                    userImageUrl = url
                }
            }.addOnFailureListener {
                Log.i(LOG_TAG, it.toString())
            }
        return temp
    }

    @JvmStatic
    private fun initPostData(map: Map<String, Any>): PostData =
        PostData(
            id = map["id"].toString(),
            urlVisual = map["urlVisual"].toString(),
            textTitle = map["textTitle"].toString(),
            datePost = convertToDate(map["datePost"]),
            imgThumb = map["imgThumb"].toString(),
            uid = map["uid"].toString()
        )

    @JvmStatic
    fun getComments(key: String): List<Comments> {
        val commentsList = mutableListOf<Comments>()
        state.onCommentSuccess(StatusEnums.LOADING)
        comRef.whereEqualTo("pid", key)
            .whereEqualTo("tag", "main").get()
            .addOnSuccessListener { rs ->
                if (!rs.isEmpty) for (e in rs) {
                    var bol = false
                    likeRef.whereEqualTo("id", e.data["id"])
                        .whereGreaterThanOrEqualTo("uid", Firebase.auth.uid.toString()).get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                bol = true
                            }
                            var i = 0
                            comRef.whereEqualTo("uid", e.data["id"].toString()).get()
                                .addOnSuccessListener { mh ->
                                    if (!mh.isEmpty) {
                                        i = mh.size()
                                    }
                                    userRef.whereEqualTo(
                                        "id",
                                        e.data["uid"].toString()
                                    ).get().addOnSuccessListener { it1 ->
                                        for (u in it1) {
                                            commentsList.add(
                                                Comments(
                                                    e.data["id"].toString(),
                                                    e.data["content"].toString(),
                                                    convertToDate(e.data["date"]),
                                                    e.data["tag"].toString(),
                                                    u.data["firstname"].toString() + " " + u.data["lastname"].toString(),
                                                    u.data["urlImg"].toString(),
                                                    e.data["like"].toString(),
                                                    bol,
                                                    i
                                                )
                                            )
                                        }
                                        Log.i(LOG_TAG, commentsList.toString())
                                        state.onCommentSuccess(StatusEnums.DONE)
                                    }.addOnFailureListener { ex ->
                                        Log.i(LOG_TAG, ex.stackTraceToString())
                                        state.onCommentSuccess(StatusEnums.ERROR)
                                    }
                                }
                        }
                }
            }.addOnFailureListener {
                Log.i(LOG_TAG, it.stackTraceToString())
            }
        return commentsList
    }

    @JvmStatic
    fun writePost(title: String, uri: Uri?, aUri: Uri?) {
        val id = generateId()
        if (uri != null) {
            val ref = storeRef.child("postimage/${id}")
            val t = ref.putFile(uri)
            t.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    val data = mutableMapOf(
                        "id" to generateId(),
                        "textTitle" to title,
                        "datePost" to Timestamp(Calendar.getInstance().time),
                        "imgThumb" to it.result.toString(),
                        "urlVisual" to "",
                        "uid" to Firebase.auth.uid.toString()
                    )
                    val aRef = storeRef.child("tracks/$id")
                    if (aUri != null) {
                        val aT = aRef.putFile(aUri)

                        aT.continueWithTask { aTask ->
                            if (!aTask.isSuccessful) {
                                aTask.exception?.let { aIt ->
                                    throw aIt
                                }
                            }
                            aRef.downloadUrl
                        }.addOnCompleteListener { cTask ->
                            data["urlVisual"] = cTask.result.toString()
                            postsRef.document(id).set(data).addOnSuccessListener { itp ->
                                Log.i(LOG_TAG, itp.toString())
                            }.addOnFailureListener { ex ->
                                Log.i(LOG_TAG, ex.stackTraceToString())
                            }
                        }
                    } else {
                        postsRef.document(id).set(data).addOnSuccessListener { itp ->
                            Log.i(LOG_TAG, itp.toString())
                        }.addOnFailureListener { ex ->
                            Log.i(LOG_TAG, ex.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            postsRef.document().set(
                mutableMapOf(
                    "id" to generateId(),
                    "titleText" to title,
                    "datePost" to Timestamp(Calendar.getInstance().time),
                    "imgThumb" to "",
                    "urlVisual" to "",
                    "uid" to Firebase.auth.uid.toString()
                )
            ).addOnSuccessListener { itp ->
                Log.i(LOG_TAG, itp.toString())
            }.addOnFailureListener { ex ->
                Log.i(LOG_TAG, ex.stackTraceToString())
            }

        }
    }

    @JvmStatic
    fun like(id: String, like: Int) {
        val l = "${like + 1}"
        comRef.document(id).update(
            "like", l
        ).addOnSuccessListener {
            likeRef.document(id).set(
                hashMapOf(
                    "id" to id,
                    "type" to 0,
                    "uid" to Firebase.auth.uid.toString()
                )
            )
        }
    }

    @JvmStatic
    private fun convertToDate(date: Any?): Date {
        val tempDate = date as Timestamp
        return tempDate.toDate()
    }

    @JvmStatic
    private fun generateId(): String {
        val authToken = Firebase.auth.uid.toString().substring(0, 5)
        var idToken = ""
        Log.i(LOG_TAG, authToken)
        for (i in 0..4) {
            idToken += nChars[Random.nextInt(0, nChars.length)]
        }
        return "${Firebase.auth.uid.toString().subSequence(0, 5)}$idToken"
    }

    @JvmStatic
    fun getNotifications(id: String): List<Notification> {
        val notifications = mutableListOf<Notification>()
        callBackState.onListNotifyState(StatusEnums.LOADING)
        noRef.whereEqualTo("id", id).get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (e in it) {
                    userRef.whereEqualTo("id", e.data["uid"]).get().addOnSuccessListener { il ->
                        if (!il.isEmpty) for (d in il) {
                            notifications.add(
                                Notification(
                                    id = e.data["id"].toString(),
                                    content = e.data["content"].toString(),
                                    user = User(
                                        id = d.data["id"].toString(),
                                        firstname = d.data["firstname"].toString(),
                                        lastname = d.data["lastname"].toString(),
                                        urlImg = d.data["urlImg"].toString(),
                                    ),
                                    type = 0,
                                    date = SimpleDateFormat(
                                        "MM/dd/yy",
                                        Locale.US
                                    ).format(convertToDate(e.data["date"]))
                                )
                            )
                        }
                        callBackState.onListNotifyState(StatusEnums.DONE)
                    }
                }
                callBackState.onListNotifyState(StatusEnums.LOADING)
            }
            requestRef.whereEqualTo("rid", id).get().addOnSuccessListener { ir ->
                if (!ir.isEmpty) {
                    for (e in ir) {
                        userRef.whereEqualTo("id", e.data["uid"]).get().addOnSuccessListener { il ->
                            if (!il.isEmpty) for (d in il) {
                                val content =
                                    d.data["firstname"].toString() + d.data["lastname"].toString()
                                notifications.add(
                                    Notification(
                                        id = e.data["id"].toString(),
                                        content = content,
                                        type = 1,
                                        user = User(
                                            id = d.data["id"].toString(),
                                            firstname = d.data["firstname"].toString(),
                                            lastname = d.data["lastname"].toString(),
                                            urlImg = d.data["urlImg"].toString(),
                                        ),
                                        date = SimpleDateFormat(
                                            "MM/dd/yy",
                                            Locale.US
                                        ).format(convertToDate(e.data["date"]))
                                    )
                                )
                            }
                            callBackState.onListNotifyState(StatusEnums.DONE)
                        }.addOnFailureListener {
                            callBackState.onListNotifyState(StatusEnums.ERROR)
                        }
                    }
                } else callBackState.onListNotifyState(StatusEnums.EMPTY)
            }
        }
        return notifications
    }

    @JvmStatic
    fun getUsers(): List<User> {
        val list = mutableListOf<User>()
        callBackState.onListenLoadState(StatusEnums.LOADING)
        frRef.whereGreaterThanOrEqualTo("uid", Firebase.auth.uid.toString().subSequence(0, 10))
            .get()
            .addOnSuccessListener { frList ->
                Log.i(LOG_TAG, frList.toString())
                if (!frList.isEmpty) for (frd in frList) {
                    userRef.whereNotEqualTo("id", frd.data["id"].toString())
                        .whereGreaterThan("id", Firebase.auth.uid.toString()).get()
                        .addOnSuccessListener { frL ->
                            if (!frL.isEmpty) for (fr in frL) {
                                list.add(
                                    User(
                                        id = fr.data["id"].toString(),
                                        firstname = fr.data["firstname"].toString(),
                                        lastname = fr.data["lastname"].toString(),
                                        urlImg = fr.data["urlImg"].toString(),
                                        birthday = convertToDate(fr.data["birthday"]),
                                        isArtist = fr.data["artist"] as Boolean,
                                        gender = fr.data["gender"].toString().toInt()
                                    )
                                )
                            }
                            Log.i(LOG_TAG, list.toString() + "something")
                            callBackState.onListenLoadState(StatusEnums.DONE)
                        }.addOnFailureListener {
                            callBackState.onListenLoadState(StatusEnums.ERROR)
                            Log.i(LOG_TAG, it.stackTraceToString())
                        }
                }
                else {
                    userRef
                        .whereNotEqualTo("id", Firebase.auth.uid).get()
                        .addOnSuccessListener { frL ->
                            if (!frL.isEmpty) for (fr in frL) {
                                list.add(
                                    User(
                                        id = fr.data["id"].toString(),
                                        firstname = fr.data["firstname"].toString(),
                                        lastname = fr.data["lastname"].toString(),
                                        urlImg = fr.data["urlImg"].toString(),
                                        birthday = convertToDate(fr.data["birthday"]),
                                        isArtist = fr.data["isArtist"] as Boolean,
                                        gender = fr.data["gender"].toString().toInt()
                                    )
                                )
                            }
                            Log.i(LOG_TAG, list.toString() + "something")
                            callBackState.onListenLoadState(StatusEnums.DONE)
                        }.addOnFailureListener {
                            callBackState.onListenLoadState(StatusEnums.ERROR)
                            Log.i(LOG_TAG, it.stackTraceToString())
                        }
                }
            }.addOnFailureListener {
                Log.i(LOG_TAG, it.stackTraceToString())
            }
        return list
    }

    @JvmStatic
    fun countNum(id: String) {
        frRef.whereGreaterThanOrEqualTo("id", id).get().addOnSuccessListener {
            if (!it.isEmpty)
                callBackState.getFriendNum(it.count())
        }
        postsRef.whereGreaterThanOrEqualTo("id", id.substring(0, 5)).get().addOnSuccessListener {
            if (!it.isEmpty)
                callBackState.getPostsNum(it.count())
        }
        trackRef.whereGreaterThanOrEqualTo("id", id.substring(0, 5)).get().addOnSuccessListener {
            if (!it.isEmpty)
                callBackState.getSongsNum(it.count())
        }
    }

    @JvmStatic
    fun countNotification(n: (n: Int) -> Unit) {
        noRef.whereEqualTo("id", Firebase.auth.uid.toString()).get().addOnSuccessListener {
            if (!it.isEmpty)
                n(it.size())
        }
    }

    @JvmStatic
    fun acceptFriend(id: String) {
        frRef.document(Firebase.auth.uid.toString().substring(0, 5) + id.substring(6, 10)).set(
            hashMapOf(
                "id" to Firebase.auth.uid,
                "uid" to id,
                "date" to Timestamp(Calendar.getInstance().time)
            )
        )
        requestRef.document(id).delete()
    }

    @JvmStatic
    fun reject(id: String) {

    }

    private val noRef = db.collection("notify")
    private var noListener: ListenerRegistration? = null

    @JvmStatic
    fun listenChangedToNotify() {
        noListener = requestRef.addSnapshotListener { _, e ->
            if (e != null) {
                Log.i(LOG_TAG, "listener error", e)
                return@addSnapshotListener
            }
            callBackState.listenNotification()
        }
    }

    @JvmStatic
    fun request(id: String) {
        val i = Firebase.auth.uid.toString().substring(0, 5) + id.substring(0, 5)
        requestRef.document(i).set(
            hashMapOf(
                "id" to i,
                "uid" to Firebase.auth.uid,
                "rid" to id,
                "date" to Timestamp(Calendar.getInstance().time)
            )
        )
    }

    @JvmStatic
    fun getTracks(): List<Track> {
        callBackState.onLoadListSongs(StatusEnums.LOADING)
        val list = mutableListOf<Track>()
        trackRef.get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (e in it) {
                    list.add(
                        Track(
                            id = e.data["id"].toString(),
                            url = e.data["url"].toString(),
                            name = e.data["name"].toString(),
                            urlImage = e.data["urlImage"].toString(),
                            uid = e.data["uid"].toString()
                        )
                    )
                }
                callBackState.onLoadListSongs(StatusEnums.DONE)
            } else callBackState.onLoadListSongs(StatusEnums.EMPTY)
        }.addOnFailureListener {
            callBackState.onLoadListSongs(StatusEnums.ERROR)
        }
        return list
    }

    @JvmStatic
    fun getArtist(): List<Artist> {
        val list = mutableListOf<Artist>()
        callBackState.onLoadListArtists(StatusEnums.LOADING)
        artistRef.get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (e in it) {
                    list.add(
                        Artist(
                            id = e.data["id"].toString(),
                            name = e.data["name"].toString(),
                            url = e.data["img"].toString(),
                            album = e.data["album"].toString()
                        )
                    )
                }
                callBackState.onLoadListArtists(StatusEnums.DONE)
            } else callBackState.onLoadListArtists(StatusEnums.EMPTY)
        }.addOnFailureListener {
            callBackState.onLoadListArtists(StatusEnums.ERROR)
        }
        return list
    }

    @JvmStatic
    fun getArtistDetail(id: String): Pair<List<Track>, Album> {
        callBackState.onLoadListSongs(StatusEnums.LOADING)
        val list = mutableListOf<Track>()
        val album = Album()
        trackRef.whereEqualTo("uid", id).get().addOnSuccessListener {
            if (!it.isEmpty) {
                for (e in it) {
                    list.add(
                        Track(
                            id = e.data["id"].toString(),
                            url = e.data["url"].toString(),
                            name = e.data["name"].toString(),
                            urlImage = e.data["urlImage"].toString(),
                            uid = e.data["uid"].toString()
                        )
                    )
                }
                callBackState.onLoadListSongs(StatusEnums.DONE)
            } else callBackState.onLoadListSongs(StatusEnums.EMPTY)
        }.addOnFailureListener {
            callBackState.onLoadListSongs(StatusEnums.ERROR)
        }
        return Pair<List<Track>, Album>(list, album)
    }

    fun updateUserInfo(f: String = "", l: String = "", cont: String = "", gen: Int = 0, uri: Uri?) {
        val id = generateId()
        if (uri != null) {
            val ref = storeRef.child("usersimage/${id}")
            val t = ref.putFile(uri)
            t.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener {
                val url = it.result.toString()
                userRef.document(Firebase.auth.uid.toString()).update(
                    mapOf(
                        "firstname" to f,
                        "lastname" to l,
                        "contact" to cont,
                        "gender" to gen,
                        "urlImg" to url
                    )
                ).addOnSuccessListener {
                    state.onUpdateUserSuccess(true)
                }.addOnFailureListener { }
            }
        } else {
            userRef.document(Firebase.auth.uid.toString()).update(
                mapOf(
                    "firstname" to f,
                    "lastname" to l,
                    "contact" to cont,
                    "gender" to gen,
                    "urlImg" to ""
                )
            ).addOnSuccessListener {
                state.onUpdateUserSuccess(true)
            }.addOnFailureListener { }
        }
    }

    fun getSpecificPost(id: String): List<Post> {
        val posts = mutableListOf<Post>()
        val postData = mutableListOf<PostData>()
        state.onGetUsersPosts(StatusEnums.LOADING)
        postsRef.whereEqualTo("uid", id).limit(20)
            .get()
            .addOnSuccessListener { rs ->
                if (!rs.isEmpty) {
                    for (doc in rs) {
                        posts.add(initPost(initPostData(doc.data)))
                    }
                     state.onGetUsersPosts(StatusEnums.DONE)
                } else state.onGetUsersPosts(StatusEnums.EMPTY)

            }.addOnFailureListener {
                state.onGetUsersPosts(StatusEnums.ERROR)
            }
        return posts
    }
}


