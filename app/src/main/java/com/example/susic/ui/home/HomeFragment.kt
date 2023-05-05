package com.example.susic.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.data.PlayerViewModel
import com.example.susic.data.Post
import com.example.susic.databinding.FragmentHomeBinding
import com.example.susic.network.DB
import com.example.susic.network.LOG_TAG
import com.example.susic.player.MediaPlayerHolder

class HomeFragment() : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sMediaPlayerHolder: MediaPlayerHolder
    private val viewModel: SusicViewModel by activityViewModels()

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
                if (recyclerView.childCount > 0)
                    if (dy == recyclerView[0].measuredHeight - recyclerView.measuredHeight)
                        binding.loadMorePrg.visibility = View.VISIBLE


            }
        })

        binding.swipeLayout.setOnRefreshListener {
            viewModel.aGetPosts()
        }

        binding.viewModel = viewModel
        binding.recView.visibility = View.GONE
        viewModel.posts.observe(this.viewLifecycleOwner) {
            adapter.submitList(it)
        }
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
                    binding.swipeLayout.isRefreshing = false
                }

                else -> {
                    binding.loadImg.visibility = View.GONE
                    binding.recView.visibility = View.GONE
                    binding.img.visibility = View.VISIBLE
                    binding.swipeLayout.isRefreshing = false
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