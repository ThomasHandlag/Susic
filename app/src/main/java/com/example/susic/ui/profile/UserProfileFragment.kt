package com.example.susic.ui.profile

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
import com.example.susic.R
import com.example.susic.SusicViewModel
import com.example.susic.data.Post
import com.example.susic.databinding.FragmentUserProfileBinding
import com.example.susic.databinding.PpItemBinding

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
        bind.archiveT.text = context?.getString(
            R.string.archive_lb,
            if (viewModel.currentViewUser.value?.isArtist == true) "Artist" else "Noob"
        )
        // Inflate the layout for this fragment
        return bind.root
    }

    companion object {

    }
}

class ProfPostAdapter : ListAdapter<Post, PPViewHolder>(PPDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PPViewHolder {
        return PPViewHolder(PpItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: PPViewHolder, p: Int) {
        val data = getItem(p)
    }

}

class PPViewHolder(private val bind: PpItemBinding) : RecyclerView.ViewHolder(bind.root) {

}

class PPDiff : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(o: Post, n: Post): Boolean {
        return o == n
    }

    override fun areContentsTheSame(o: Post, n: Post): Boolean {
        return o == n
    }

}