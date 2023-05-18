package com.example.susic

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.data.Track
import com.example.susic.databinding.FragmentSearchBinding
import com.example.susic.databinding.SearchItemBinding

class Search(private val aShowPlayer: (track: Track) -> Unit) : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SusicViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        val adapter = SearchAdapter(requireContext(), aShowPlayer)
        binding.recView.adapter = adapter
        viewModel.sTrack.observe(this.viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.sTrackState.observe(this.viewLifecycleOwner) {
            when (it) {
                StatusEnums.LOADING -> {
                    binding.prg.visibility = View.VISIBLE
                    binding.recView.visibility = View.GONE
                    binding.empText.visibility = View.GONE
                }

                StatusEnums.DONE -> {
                    binding.prg.visibility = View.GONE
                    binding.recView.visibility = View.VISIBLE
                    binding.empText.visibility = View.GONE
                }

                StatusEnums.EMPTY -> {
                    binding.prg.visibility = View.GONE
                    binding.recView.visibility = View.GONE
                    binding.empText.visibility = View.VISIBLE
                }

                else -> {
                    binding.prg.visibility = View.GONE
                    binding.recView.visibility = View.GONE
                    binding.empText.visibility = View.VISIBLE
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    class SearchAdapter(
        private val context: Context,
        private val aShowPlayer: (track: Track) -> Unit
    ) : ListAdapter<Track, SearchVHolder>(DiffItem()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVHolder {
            return SearchVHolder(
                SearchItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: SearchVHolder, pos: Int) {
            holder.bind(getItem(pos), context, aShowPlayer)
        }
    }

    class SearchVHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Track, context: Context, aShowPlayer: (track: Track) -> Unit) {
            binding.textView.text = context.getString(R.string.user_name, item.name)
            binding.card.setOnClickListener {
                aShowPlayer(item)
            }
        }
    }

    class DiffItem : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(o: Track, n: Track): Boolean {
            return o == n
        }

        override fun areContentsTheSame(o: Track, n: Track): Boolean {
            return o == n
        }

    }
}

data class SearchItem(val id: String = "", val name: String = "")