package com.example.susic.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.example.susic.data.Comments
import com.example.susic.databinding.CommentItemBinding
import com.example.susic.databinding.CommentSectionBinding
import com.example.susic.databinding.SettingLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentSheet :
    BottomSheetDialogFragment() {
    private lateinit var commentSheet: CommentSectionBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        commentSheet = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.comment_section, container, false
        )
        commentSheet.comTextEdit.prefixTextView.setOnClickListener {
            with(viewModel) {
                repId.value = ""
                repName.value = ""
            }
        }
        val like: (key: String, like: String) -> Unit = { it, like ->
            viewModel.like(it, like)
        }
        viewModel.repName.observe(this) {
            commentSheet.comTextEdit.prefixText = it
        }
        //set id for reply
        val writeComment: (id: String, name: String) -> Unit = { id, name ->
            commentSheet.comTextEdit.requestFocus()
            viewModel.repId.value = id
            viewModel.repName.value = name
        }
        commentSheet.addBtn.setOnClickListener {
            viewModel.setComment(
                commentSheet.comTextEdit.editText?.text.toString(),
                if (viewModel.repId.value == null || viewModel.repId.value == "") 0 else 1
            )
            //clear text
            commentSheet.comTextEdit.editText?.text?.clear()
            commentSheet.comTextEdit.prefixText = ""
        }
        val showRep: (com: Comments) -> Unit = { it ->
            viewModel.mainComments(it)
            viewModel.getReplies(it.id)
            val repSheet = ReplySheet()
            repSheet.show(requireActivity().supportFragmentManager, ReplySheet.TAG)
        }
        val adapter = CommentAdapter(like, writeComment, context, showRep)
        commentSheet.data = viewModel
        commentSheet.container.adapter = adapter
        commentSheet.container.visibility = View.GONE
        //loading
        viewModel.commentState.observe(requireActivity()) {
            when (it) {
                StatusEnums.LOADING -> {
                    commentSheet.imageView2.visibility = View.VISIBLE
                    commentSheet.container.visibility = View.GONE
                }

                StatusEnums.DONE -> {
                    commentSheet.imageView2.visibility = View.GONE
                    commentSheet.container.visibility = View.VISIBLE
                }

                else -> {
                    with(commentSheet.imageView2) {
                        visibility = View.VISIBLE
//                        setImageResource(R.drawable.ic_round_signal_wifi_off_24)
                    }
                    commentSheet.container.visibility = View.GONE
                }
            }
        }
        //apply changes
        viewModel.comments.observe(this) {
            adapter.submitList(it)
        }
        return commentSheet.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroyCommentListener()
    }

    companion object {
        const val TAG = "CommentSheet"
    }
}

class CommentAdapter(
    private val like: (id: String, like: String) -> Unit,
    private val writeComment: (id: String, name: String) -> Unit,
    private val context: Context?,
    private val showRep: (it: Comments) -> Unit
) : ListAdapter<Comments, CommentViewHolder>(CommentDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(CommentItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment, like, writeComment, context, showRep)
    }
}

class CommentViewHolder(private val binding: CommentItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        comment: Comments,
        like: (id: String, like: String) -> Unit,
        writeComment: (id: String, name: String) -> Unit,
        context: Context?,
        showRep: (it: Comments) -> Unit
    ) {
        binding.comment = comment
        if (comment.tag == "rep" || comment.repCount == 0) {
            binding.openRepBtn.visibility = View.INVISIBLE
        } else binding.openRepBtn.text =
            context?.getString(R.string.num_rep, comment.repCount.toString())
        binding.openRepBtn.setOnClickListener {
            showRep(comment)
        }
        binding.likeBtn.setOnClickListener {
            val n = comment.like.toInt() + 1
            binding.likeBtn.setIconResource(R.drawable.ic_round_thumb_up_24)
            binding.likeBtn.text = context?.getString(R.string.num_like, n.toString())
            binding.likeBtn.isClickable = false
            like(comment.id, comment.like)
        }
        //set rep data
        binding.writeComBtn.setOnClickListener {
            writeComment(comment.id, comment.user)
        }
        if (comment.liked) {
            binding.likeBtn.setIconResource(R.drawable.ic_round_thumb_up_24)
            binding.likeBtn.isClickable = false
        } else {
            binding.likeBtn.setIconResource(R.drawable.ic_outline_thumb_up_24)
            binding.likeBtn.isClickable = true
        }
    }
}

class CommentDiff : DiffUtil.ItemCallback<Comments>() {
    override fun areItemsTheSame(oldItem: Comments, newItem: Comments): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Comments, newItem: Comments): Boolean {
        return oldItem == newItem
    }
}

class ReplySheet : BottomSheetDialogFragment() {
    private lateinit var bind: SettingLayoutBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(layoutInflater, R.layout.setting_layout, container, false)
        val like: (key: String, like: String) -> Unit = { it, like ->
            viewModel.like(it, like)
        }
        val writeComment: (id: String, name: String) -> Unit = { _, _ ->

        }
        val showRep: (it: Comments) -> Unit = {}
        bind.rec.visibility = View.GONE

        val adapter = CommentAdapter(like, writeComment, context, showRep)
        bind.viewM = viewModel
        bind.rec.adapter = adapter
        viewModel.repState.observe(requireActivity()) {
            when (it) {
                StatusEnums.LOADING -> {
                    bind.imageView2.visibility = View.VISIBLE
                    bind.rec.visibility = View.GONE
                }

                StatusEnums.DONE -> {
                    bind.imageView2.visibility = View.GONE
                    bind.rec.visibility = View.VISIBLE
                }

                else -> {
                    with(bind.imageView2) {
                        visibility = View.VISIBLE
//                        setImageResource(R.drawable.ic_round_signal_wifi_off_24)
                    }
                    bind.rec.visibility = View.GONE
                }
            }
        }
        return bind.root
    }

    companion object {
        const val TAG = "RepSheet"
    }
}