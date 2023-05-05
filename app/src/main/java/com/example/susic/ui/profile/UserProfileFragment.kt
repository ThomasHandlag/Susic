package com.example.susic.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.data.Post
import com.example.susic.databinding.FragmentUserProfileBinding
import com.example.susic.databinding.ListPostBinding
import com.example.susic.databinding.PpItemBinding
import com.example.susic.network.LOG_TAG
import com.example.susic.userImg

class UserProfileFragment : Fragment() {
    private lateinit var bind: FragmentUserProfileBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false)
        bind.viewM = viewModel
        viewModel.currentViewUser.value?.id?.let { viewModel.getSpecificPost(it) }
        bind.archiveT.text = context?.getString(
            R.string.archive_lb,
            if (viewModel.currentViewUser.value?.isArtist == true) "Artist" else "Noob"
        )
        bind.numFr.text =
            getString(R.string.num_fr, viewModel.currentViewUser.value?.numFr.toString())
        bind.numPosts.text =
            getString(R.string.num_post, viewModel.currentViewUser.value?.numPost.toString())
        bind.numSongs.text =
            getString(R.string.num_song, viewModel.currentViewUser.value?.numPost.toString())
        bind.addFrBtn.setOnClickListener {
            viewModel.currentViewUser.value?.id?.let { it1 -> viewModel.request(it1) }
        }
        val ppAdapter = ProfPostAdapter()
        viewModel.listProf.observe(this.viewLifecycleOwner) {
            Log.i(LOG_TAG, it.toString())
            ppAdapter.submitList(it)
        }
        bind.gridView.adapter = ppAdapter
        viewModel.listProfState.observe(this.viewLifecycleOwner) {
            when (it) {
                StatusEnums.LOADING -> {
                    bind.gridView.visibility = View.GONE
                    bind.prg.visibility = View.VISIBLE
                    bind.img.visibility - View.GONE
                    bind.text.visibility = View.GONE
                }

                StatusEnums.DONE -> {
                    bind.prg.visibility = View.GONE
                    bind.gridView.visibility = View.VISIBLE
                    bind.img.visibility - View.GONE
                    bind.text.visibility = View.GONE
                }
                StatusEnums.EMPTY -> {
                    bind.prg.visibility = View.GONE
                    bind.gridView.visibility = View.GONE
                    bind.img.visibility = View.GONE
                    bind.text.visibility = View.VISIBLE
                }

                else -> {
                    bind.gridView.visibility = View.GONE
                    bind.img.visibility = View.VISIBLE
                    bind.prg.visibility = View.GONE
                    bind.text.visibility = View.GONE
                }
            }
        }
        // Inflate the layout for this fragment
        return bind.root
    }

    companion object {

    }
}

class ProfPostAdapter : ListAdapter<Post, PPViewHolder>(PPDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PPViewHolder {
        return PPViewHolder(ListPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PPViewHolder, p: Int) {
        val data = getItem(p)
        holder.bind(data)
    }

}

class PPViewHolder(private val bind: ListPostBinding) : RecyclerView.ViewHolder(bind.root) {
    fun bind(post: Post) {
        bind.data = post
        Log.i(LOG_TAG, post.toString())
    }
}

class PPDiff : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(o: Post, n: Post): Boolean {
        return o == n
    }

    override fun areContentsTheSame(o: Post, n: Post): Boolean {
        return o == n
    }

}