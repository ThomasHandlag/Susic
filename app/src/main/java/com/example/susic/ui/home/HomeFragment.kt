package com.example.susic.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.data.PlayerViewModel
import com.example.susic.data.Post
import com.example.susic.databinding.FragmentHomeBinding
import com.example.susic.network.DB
import com.example.susic.network.LOG_TAG
import com.example.susic.player.MediaPlayerHolder

class HomeFragment() : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sMediaPlayerHolder: MediaPlayerHolder
    private val viewModel: PlayerViewModel by lazy {
        ViewModelProvider(requireActivity())[PlayerViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sMediaPlayerHolder = MediaPlayerHolder.getInstance()
    }

    override fun onResume() {
        super.onResume()
        if (!::sMediaPlayerHolder.isInitialized) {
            sMediaPlayerHolder = MediaPlayerHolder.getInstance()
        } else {
            sMediaPlayerHolder.restoreOrStartSeekBarProgress()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val commentSheet = CommentSheet()
        val commentAction = object : CommentActionListener {
            override fun onShowSection(id: String) {
                Log.i(LOG_TAG, id)
                viewModel.postId.value = id
                viewModel.getComments(id)
            }
        }
        val adapter = PostAdapter(
            commentSheet,
            requireActivity().application,
            requireActivity().supportFragmentManager,
            commentAction
        )
        binding.recView.adapter = adapter
        binding.recView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        binding.recView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {

            }

        })
        viewModel.posts.observe(requireActivity()) {
            adapter.submitList(it)
        }
        binding.viewModel = viewModel
        binding.recView.visibility = View.GONE
        viewModel.loadState.observe(requireActivity()) {
            when (it) {
                StatusEnums.LOADING -> {
                    binding.loadImg.visibility = View.VISIBLE
                    binding.recView.visibility = View.GONE
                    binding.img.visibility = View.GONE
                }
                StatusEnums.DONE -> {
                    binding.loadImg.visibility = View.GONE
                    binding.recView.visibility = View.VISIBLE
                    binding.img.visibility = View.GONE
                }
                else -> {
                    binding.loadImg.visibility = View.GONE
                    binding.recView.visibility = View.GONE
                    binding.img.visibility = View.VISIBLE
                }
            }
        }

        //Inflate layout
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        sMediaPlayerHolder.stopUpdateSeekBar()
    }

    override fun onStop() {
        super.onStop()
        sMediaPlayerHolder.stopUpdateSeekBar()
    }
}

interface CommentActionListener {
    fun onShowSection(id: String)
}