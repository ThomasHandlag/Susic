package com.example.susic

import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.susic.data.PostData
import com.example.susic.ui.home.PostAdapter
import java.text.ParseException
import java.util.*

@BindingAdapter("posts")
fun RecyclerView.posts(data: List<PostData>) {
    val adapter = adapter as PostAdapter
    adapter.submitList(data)
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
fun ImageView.userImg(imgUrl: String) {
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
fun TextView.datePosted(str: String) {
    val inputFormat = SimpleDateFormat("MMMMM dd, yyyy 'at' HH:mm:ss aaa z", Locale.US)
    val outputFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
    try {
        val date = inputFormat.parse(str)
        val outputDateStr = outputFormat.format(date)
        text = context.getString(R.string.date_post, outputDateStr)
    } catch (exception: ParseException) {
        exception.printStackTrace()
    }
}
