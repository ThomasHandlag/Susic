package com.example.susic

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.susic.data.Comments
import com.example.susic.data.Post
import com.example.susic.data.PostData
import com.example.susic.data.User
import com.example.susic.ui.artist.ArtistFragment
import com.example.susic.ui.home.CommentAdapter
import com.example.susic.ui.home.PostAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.util.*

@BindingAdapter("posts")
fun RecyclerView.posts(posts: List<Post>) {
    val adapter = adapter as PostAdapter
    adapter.submitList(posts)
}

@BindingAdapter("comments")
fun RecyclerView.comments(comments: List<Comments>) {
    val adapter = adapter as CommentAdapter
    adapter.submitList(comments)
}
@BindingAdapter("users")
fun RecyclerView.users(list: List<User>) {
    val adapter = adapter as ArtistFragment.ItemAdapter
    adapter.submitList(list)
}

@BindingAdapter("imgThumb")
fun ImageView.imgThumb(imgUrl: String) {
    imgUrl.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_img)
            )
            .into(this)
    }
}

@BindingAdapter("userImg")
fun ShapeableImageView.userImg(imgUrl: String) {
    imgUrl.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_round_person_24)
                    .error(R.drawable.ic_round_person_24)
            )
            .into(this)
    }
}

@BindingAdapter("ownerName")
fun TextView.ownerName(str: String) {
    text = context.getString(R.string.user_name, str)
}

@BindingAdapter("titleThumb")
fun TextView.titleThumb(title: String) {
    text = context.getString(R.string.thumb_text, title)
}

@BindingAdapter("datePosted")
fun TextView.datePosted(str: Date) {
    val outputFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
    val outputDateStr = outputFormat.format(str)
    text = context.getString(R.string.date_post, outputDateStr)

}

@BindingAdapter("state")
fun ImageView.state(st: Boolean) {
    visibility = if (st) View.GONE
    else View.VISIBLE
}

@BindingAdapter("badgeStr")
fun MaterialButton.badgeStr(str: String){
    text = context.getString(R.string.badge_str, str)
}
