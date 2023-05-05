package com.example.susic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.susic.data.Artist
import com.example.susic.data.Track
import com.example.susic.databinding.FragmentDetailBinding
import com.example.susic.databinding.PpItemBinding
import com.example.susic.ui.library.SongAdapter

class DetailFragment(private val art: Artist, private val showPlayer: (track: Track) -> Unit) : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: SusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        binding.art = art
        viewModel.getArtistInfo(art.id)
        val adapter = SongAdapter(showPlayer)
        binding.recView.adapter = adapter
        viewModel.artistDetail.observe(this.viewLifecycleOwner) {
            adapter.submitList(it.first)
        }
        viewModel.trackLoadState.observe(this.viewLifecycleOwner) {
            when(it) {
                StatusEnums.EMPTY -> {
                    with(binding) {
                        recView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = getText(R.string.empty_lb)
                        prgBar.visibility = View.GONE
                    }
                }

                StatusEnums.ERROR -> {
                    with(binding) {
                        recView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        prgBar.visibility = View.GONE
                    }
                }

                StatusEnums.LOADING -> {
                    with(binding) {
                        recView.visibility = View.GONE
                        emptyText.visibility = View.GONE
                        prgBar.visibility = View.VISIBLE
                    }
                }

                else -> {
                    with(binding) {
                        recView.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE
                        prgBar.visibility = View.GONE
                    }
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }
}