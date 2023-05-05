package com.example.susic.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.databinding.FragmentProfileBinding
import com.example.susic.userImg
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_profile, container, false)
        binding.viewModel = viewModel
        val mediaPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                binding.userImg.visibility = View.GONE
                binding.tempImg.visibility = View.VISIBLE
                binding.tempImg.setImageURI(it)
                viewModel.file.value = it
            } else {
                Toast.makeText(context, R.string.cancel_lb, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.userImg.setOnClickListener {
            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
        viewModel._updateUserState.observe(this.viewLifecycleOwner) {
            if (it) {
                viewModel.iGetCurrentUser()
                Toast.makeText(requireContext(), R.string.nof_user_update, Toast.LENGTH_SHORT)
                    .show()
            }
            else Toast.makeText(requireContext(), R.string.nof_user_update, Toast.LENGTH_SHORT)
                .show()
        }
        val gender = viewModel.currentUser.value?.gender ?: 0
        var selectedItem = 0
        with(binding) {
            val list = listOf("Male", "Female")
            val textVAdapter = ArrayAdapter(requireContext(), R.layout.gender_item, list)
            (genderCh.editText as MaterialAutoCompleteTextView).setAdapter(textVAdapter)
            (genderCh.editText as MaterialAutoCompleteTextView).setText(
                textVAdapter.getItem(gender).toString(), false
            )
            (genderCh.editText as MaterialAutoCompleteTextView).setOnItemClickListener { _, _, i, _ ->
                selectedItem = i
            }
        }
        val name =
            viewModel.currentUser.value?.firstname + " " + viewModel.currentUser.value?.lastname
        binding.userName.text = getString(R.string.user_name, name)
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
        binding.saveBtn.setOnClickListener {
            val fname = binding.fName.editText?.text.toString() + ""
            val lname = binding.lName.editText?.text.toString() + ""
            val cont = binding.contact.editText?.text.toString() + ""
            viewModel.updateUserInfo(
                fname,
                lname,
                cont,
                selectedItem
            )
        }
        binding.cancelBtn.setOnClickListener {

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
    }
}