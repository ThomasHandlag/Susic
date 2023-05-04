package com.example.susic.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.databinding.FragmentProfileBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(this)[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_profile, container, false)
        binding.viewModel = viewModel
        viewModel.iGetCurrentUser()
        with(binding) {
            val list = listOf("male", "female")
            val textVAdapter = ArrayAdapter(requireContext(), R.layout.gender_item, list)
            (genderCh.editText as MaterialAutoCompleteTextView).setAdapter(textVAdapter)
        }
        viewModel.headerState.observe(requireActivity()) {
            with(binding) {
                when (it) {
                    StatusEnums.DONE -> {
                        lnTop.visibility = View.VISIBLE
                        edV.visibility = View.VISIBLE
                        prgBar.visibility = View.GONE
                    }
                    StatusEnums.LOADING -> {
                        lnTop.visibility = View.GONE
                        edV.visibility = View.GONE
                        prgBar.visibility = View.VISIBLE
                    }
                    else -> {
                        edV.visibility = View.GONE
                        prgBar.visibility = View.VISIBLE
                        lnTop.visibility = View.GONE
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
    }
}