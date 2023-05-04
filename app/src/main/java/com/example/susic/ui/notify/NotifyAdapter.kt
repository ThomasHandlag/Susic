package com.example.susic.ui.notify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.data.User
import com.example.susic.databinding.NotifyItemBinding
import com.example.susic.ui.home.PostAdapter

class NotifyAdapter(private val choice: (id: String, ch: Boolean) -> Unit): ListAdapter<Notification, NotifyHolder>(NotifyDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifyHolder {
        return NotifyHolder(NotifyItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: NotifyHolder, pos: Int) {
        val nof = getItem(pos)
        holder.bind(nof, choice)
    }
}

class NotifyHolder(private val binding: NotifyItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(nof: Notification, choice: (id: String, ch: Boolean) -> Unit) {
        with(binding) {
            if (nof.type == 1) {
                rejBtn.visibility = View.VISIBLE
                acBtn.visibility = View.VISIBLE
                notifyTextCaption.visibility = View.GONE
            } else {
                rejBtn.visibility = View.GONE
                acBtn.visibility = View.GONE
                notifyTextCaption.visibility = View.VISIBLE
            }
            rejBtn.setOnClickListener {
                choice(nof.id, false)
            }
            acBtn.setOnClickListener {
                choice(nof.id, true)
            }
        }
    }
}

class NotifyDiff : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}

data class Notification(
    val id: String = "",
    val content: String = "",
    val type: Int = 0,
    val user: User,
    val uid: String = "",
    val date: String = ""
)