package com.example.susic.ui.home

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.PlayerState
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.data.Post
import com.example.susic.data.PostData
import com.example.susic.databinding.PostItemBinding
import com.example.susic.network.DB
import com.example.susic.player.MediaPlayerHolder

class PostAdapter(
    private val commentSheet: CommentSheet,
    private val application: Application,
    private val fragmentManager: FragmentManager,
    private val commentAction: CommentActionListener
) :
    ListAdapter<Post, PostAdapter.PostViewHolder>(DiffUtilPost()) {
    private val sMediaPlayerHolder = MediaPlayerHolder.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(PostItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(
            post,
            sMediaPlayerHolder,
            commentSheet,
            application,
            fragmentManager,
            commentAction
        )
    }

    class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            post: Post,
            sMediaPlayerHolder: MediaPlayerHolder,
            commentSheet: CommentSheet,
            application: Application,
            fragmentManager: FragmentManager,
            commentAction: CommentActionListener
        ) {
            with(binding) {
                binding.post = post
                if (post.urlVisual == "") pauseBtn.visibility = View.INVISIBLE
                if (sMediaPlayerHolder.currentPostIndex == adapterPosition && sMediaPlayerHolder.currentPostIndex != null) {
                    with(sMediaPlayerHolder) {
                        if (sMediaPlayerHolder.sReadyToPlay) {
                            audioPrg.max = sMediaDuration
                        }
                        when (state) {
                            PlayerState.PLAYING -> pauseBtn.setIconResource(R.drawable.ic_round_pause_circle_24)
                            else -> pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
                        }
                        audioPrg.progress = playerPosition
                    }
                    audioPrg.visibility = View.VISIBLE
                } else audioPrg.visibility = View.INVISIBLE

                sMediaPlayerHolder.sPostControllerInterface = object : PostController {
                    override fun onComplete() {
                        pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
                    }
                }

                if (post.imgThumb == "") {
                    audioPrg.visibility = View.GONE
                    imgViewThumb.visibility = View.GONE
                    visualizer.visibility = View.VISIBLE
                }
                //show comment section
                comBtn.setOnClickListener {
                    commentAction.onShowSection(post.id)
                    with(commentSheet) {
                        show(fragmentManager, CommentSheet.TAG)
                    }
                }
                //pause, resume, start media player
                pauseBtn.setOnClickListener {
                    audioPrg.visibility = View.VISIBLE
                    sMediaPlayerHolder.setUpMediaURL(post.urlVisual, adapterPosition)
                    audioPrg.max = sMediaPlayerHolder.sMediaDuration
                    startUpdateSeekBarPosition(sMediaPlayerHolder)
                    with(sMediaPlayerHolder) {
                        resumeOrPause()
                        when (state) {
                            PlayerState.PLAYING -> pauseBtn.setIconResource(R.drawable.ic_round_pause_circle_24)
                            else -> pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
                        }
                    }
                }
                //show options for the post
                actionBtn.setOnClickListener {
                    val popupMenu = PopupMenu(application.applicationContext, actionBtn)
                    popupMenu.menuInflater.inflate(R.menu.action_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener {
                        when (it.itemId) {
                            1 -> TODO("implement block post action")
                            else -> TODO("implement save post action")
                        }
                    }
                    popupMenu.show()
                }
                //seekbar listener
                audioPrg.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, pos: Int, p2: Boolean) {
                            Log.i("Adapter", "$pos")
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            Log.i("StartTrackingTouch", "${seekBar?.progress}")
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            if (seekBar != null && sMediaPlayerHolder.currentPostIndex == adapterPosition) {
                                sMediaPlayerHolder.seekTo(seekBar.progress)
                            }
                        }

                    })
                //run in UI thread
                executePendingBindings()
            }
        }

        private fun startUpdateSeekBarPosition(sMediaPlayerHolder: MediaPlayerHolder) {
            with(binding) {
                audioPrg.postDelayed(object : Runnable {
                    override fun run() {
                        if (adapterPosition == sMediaPlayerHolder.currentPostIndex)
                            audioPrg.progress = sMediaPlayerHolder.playerPosition
                        audioPrg.postDelayed(this, 0)
                    }
                }, 0)
            }
        }
    }

    class DiffUtilPost : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}


//post callback interface
interface PostController {
    fun onComplete()
}

