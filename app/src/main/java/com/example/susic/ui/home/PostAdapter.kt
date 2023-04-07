package com.example.susic.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.R
import com.example.susic.data.PostData
import com.example.susic.databinding.PostItemBinding
import com.example.susic.player.MediaPlayerHolder

class PostAdapter : ListAdapter<PostData, PostViewHolder>(DiffUtilPost()) {
    val sMediaPlayerHolder = MediaPlayerHolder.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(PostItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post, sMediaPlayerHolder)
    }
}

class PostViewHolder(private val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        post: PostData,
        sMediaPlayerHolder: MediaPlayerHolder
    ) {
        binding.post = post
        val sPostController = object : PostController {
            override fun updateSeekbarCallBack(pos: Int) {
                binding.audioPrg.progress = pos
            }
        }
        sMediaPlayerHolder.sPostControllerInterface = sPostController

        if (post.imgThumb == "") {
            binding.audioPrg.visibility = View.GONE
            binding.imgViewThumb.visibility = View.GONE
            binding.visualizer.visibility = View.VISIBLE
        }
        //show comment section
        binding.comBtn.setOnClickListener {
        }
        //pause media player
        with(binding.pauseBtn) {
            setIconResource(R.drawable.ic_round_pause_circle_24)
            setOnClickListener {
                with(sMediaPlayerHolder) {
                    if (!isPlay) play() else resumeOrPause()
                }
            }
        }
        //run in UI thread
        binding.executePendingBindings()
    }

}

class DiffUtilPost : DiffUtil.ItemCallback<PostData>() {
    override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
        return oldItem == newItem
    }
}

interface PostController {
    fun updateSeekbarCallBack(pos: Int)
}
