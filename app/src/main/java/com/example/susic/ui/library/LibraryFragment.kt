package com.example.susic.ui.library

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.susic.DetailFragment
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.data.Artist
import com.example.susic.data.Track
import com.example.susic.databinding.FragmentLibraryBinding
import com.example.susic.databinding.NotifyItemBinding
import com.example.susic.databinding.PpItemBinding
import com.example.susic.databinding.SongItemBinding
import com.example.susic.ui.notify.Notification

class LibraryFragment(
    private val showPlayer: (track: Track) -> Unit,
    private val showDetail: (fragment: Fragment) -> Unit
) : Fragment() {
    private lateinit var binding: FragmentLibraryBinding
    private val viewModel: SusicViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {
            getArtists()
            getTracks()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library, container, false)
        val songAdapter = SongAdapter(showPlayer)
        val artistAdapter = ArtistAdapter(showDetail, showPlayer)
        with(binding) {
            artRec.adapter = artistAdapter
            songRec.adapter = songAdapter
        }
        viewModel.artists.observe(this.viewLifecycleOwner) {
            artistAdapter.submitList(it)
        }
        viewModel.tracks.observe(this.viewLifecycleOwner) {
            songAdapter.submitList(it)
        }
        viewModel.artistLoadState.observe(this.viewLifecycleOwner) {
            when (it) {
                StatusEnums.EMPTY -> {
                    with(binding) {
                        artRec.visibility = View.GONE
                        empText.visibility = View.VISIBLE
                        loadMorePrg.visibility = View.GONE
                    }
                }

                StatusEnums.ERROR -> {
                    with(binding) {
                        artRec.visibility = View.GONE
                        empText.visibility = View.VISIBLE
                        empText.text = getText(R.string.load_metadata_error)
                        loadMorePrg.visibility = View.GONE
                    }
                }

                StatusEnums.LOADING -> {
                    with(binding) {
                        artRec.visibility = View.GONE
                        empText.visibility = View.GONE
                        loadMorePrg.visibility = View.VISIBLE
                    }
                }

                else -> {
                    with(binding) {
                        artRec.visibility = View.VISIBLE
                        empText.visibility = View.GONE
                        loadMorePrg.visibility = View.GONE
                    }
                }
            }
        }
        viewModel.trackLoadState.observe(this.viewLifecycleOwner) {
            when (it) {
                StatusEnums.EMPTY -> {
                    with(binding) {
                        songRec.visibility = View.GONE
                        empText2.visibility = View.VISIBLE
                        loadMorePrg2.visibility = View.GONE
                    }
                }

                StatusEnums.ERROR -> {
                    with(binding) {
                        songRec.visibility = View.GONE
                        empText2.visibility = View.VISIBLE
                        empText2.text = getText(R.string.load_metadata_error)
                        loadMorePrg2.visibility = View.GONE
                    }
                }

                StatusEnums.LOADING -> {
                    with(binding) {
                        songRec.visibility = View.GONE
                        empText2.visibility = View.GONE
                        loadMorePrg2.visibility = View.VISIBLE
                    }
                }

                else -> {
                    with(binding) {
                        songRec.visibility = View.VISIBLE
                        empText2.visibility = View.GONE
                        loadMorePrg2.visibility = View.GONE
                    }
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }
}

class ArtistAdapter(
    private val showDetail: (fragment: Fragment) -> Unit,
    private val showPlayer: (track: Track) -> Unit
) : ListAdapter<Artist, ArtistHolder>(ArtistDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistHolder {
        return ArtistHolder(PpItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ArtistHolder, pos: Int) {
        val art = getItem(pos)
        holder.bind(art, showDetail, showPlayer)
    }
}

class ArtistHolder(private val binding: PpItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        art: Artist,
        showDetail: (fragment: Fragment) -> Unit,
        showPlayer: (track: Track) -> Unit
    ) {
        binding.data = art
        binding.cart.setOnClickListener {
            showDetail(DetailFragment(art, showPlayer))
        }
    }
}

class ArtistDiff : DiffUtil.ItemCallback<Artist>() {
    override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
        return oldItem == newItem
    }
}

class SongAdapter(private val showPlayer: (track: Track) -> Unit) :
    ListAdapter<Track, SongHolder>(SongDiff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        return SongHolder(
            SongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongHolder, pos: Int) {
        val song = getItem(pos)
        holder.bind(song, showPlayer)
    }
}

class SongHolder(private val binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(song: Track, showPlayer: (track: Track) -> Unit) {
        binding.data = song
        binding.container.setOnClickListener {
            showPlayer(song)
        }
    }
}

class SongDiff : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}

