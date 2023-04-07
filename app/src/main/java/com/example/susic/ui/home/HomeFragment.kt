package com.example.susic.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.susic.R
import com.example.susic.data.ApiViewModel
import com.example.susic.data.ApiViewModelFactor
import com.example.susic.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class HomeFragment() : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val commentSheet = CommentSheet(requireActivity().supportFragmentManager, CommentSheet.TAG)
        val db = Firebase.firestore
        val storage = Firebase.storage

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val viewModelFactory = ApiViewModelFactor(db, storage)
        binding.recView.adapter = PostAdapter()

        val viewModel = ViewModelProvider(
                this.requireActivity(),
                viewModelFactory
            )[ApiViewModel::class.java]

        binding.viewModel = viewModel
        //Inflate layout
        return binding.root
    }
}