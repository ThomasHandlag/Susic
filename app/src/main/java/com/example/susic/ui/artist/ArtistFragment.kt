package com.example.susic.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.data.User
import com.example.susic.databinding.FragmentArtistBinding
import com.example.susic.databinding.UserItemBinding
import com.example.susic.ui.profile.UserProfileFragment

class ArtistFragment(private val showDetail: (fragment: Fragment, user: User) -> Unit) :
    Fragment() {
    private lateinit var binding: FragmentArtistBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_artist, container, false)
        binding.viewM = viewModel
        val addFr: (id: String) -> Unit = {
            viewModel.request(it)
        }
        val adapter = ItemAdapter(showDetail, addFr)
        binding.recView.adapter = adapter

        val layout = binding.recView.layoutManager as StaggeredGridLayoutManager
        layout.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        layout.orientation = StaggeredGridLayoutManager.VERTICAL
        binding.recView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.recView.invalidateItemDecorations()
                }
            }
        })
        binding.recView.adapter = adapter
        viewModel.usersLoadState.observe(requireActivity()) {
            with(binding) {
                when (it) {
                    StatusEnums.DONE -> {
                        recView.visibility = View.VISIBLE
                        img.visibility = View.GONE
                        prgBar.visibility = View.GONE
                    }
                    StatusEnums.LOADING -> {
                        recView.visibility = View.GONE
                        img.visibility = View.GONE
                        prgBar.visibility = View.VISIBLE
                    }
                    else -> {
                        recView.visibility = View.GONE
                        prgBar.visibility = View.GONE
                        img.visibility = View.VISIBLE
                    }
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
    }

    class ItemAdapter(
        private val showDetail: (fragment: Fragment, user: User) -> Unit,
        private val addFr: (id: String) -> Unit
    ) : ListAdapter<User, ItemViewHolder>(ItemDiff()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder(UserItemBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, pos: Int) {
            val user = getItem(pos)
            holder.bind(user, showDetail, addFr)
        }
    }

    class ItemViewHolder(
        private val binding: UserItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            user: User,
            showDetail: (fragment: Fragment, user: User) -> Unit,
            addFr: (id: String) -> Unit
        ) {
            binding.user = user
            binding.cardView.setOnClickListener {
                showDetail(UserProfileFragment(), user)
            }
            binding.btn.setOnClickListener {
                addFr(user.id)
            }
        }
    }

    class ItemDiff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(o: User, n: User): Boolean {
            return o == n
        }

        override fun areContentsTheSame(o: User, n: User): Boolean {
            return o == n
        }
    }


}