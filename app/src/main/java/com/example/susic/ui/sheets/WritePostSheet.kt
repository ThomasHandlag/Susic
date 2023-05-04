package com.example.susic.ui.sheets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.susic.R
import com.example.susic.SusicViewModel
import com.example.susic.databinding.WritePostSheetBinding
import com.example.susic.network.LOG_TAG
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WritePostSheet : BottomSheetDialogFragment() {
    private lateinit var binding: WritePostSheetBinding
    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.write_post_sheet, container, false)

        val mediaPicker = registerForActivityResult(PickVisualMedia()) {
            if (it != null) {
                binding.imgView.setImageURI(it)
                viewModel.file.value = it
            } else {
                Toast.makeText(context, R.string.cancel_lb, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        //launch img chooser
        binding.imgView.setOnClickListener {
            mediaPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
        }
        binding.postTitle.editText?.setOnKeyListener { _, _, _ ->
            viewModel.title.value = binding.postTitle.editText?.text.toString()
            true
        }
        //audio chooser
        val launcher =  registerForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null) {
                    viewModel.audioFile.value = it
                }
        }
        binding.fileChooserBtn.setOnClickListener {
//            val i = Intent()
//            i.type = "audio/*"
//            i.action = Intent.ACTION_PICK
//            startActivity(Intent.createChooser(i, "Choose your track"))
            launcher.launch("audio/*")
        }
        //upload
        binding.button.setOnClickListener {
            if (viewModel.file.value != null) {
                viewModel.title.value = binding.postTitle.editText?.text.toString()
                viewModel.setPost()
            } else {
                Toast.makeText(context, R.string.upload_progress_lb, Toast.LENGTH_LONG).show()
            }
        }
        return binding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        //TODO: do something when this is destroyed
        Log.i(LOG_TAG, "OnDestroy")
    }

    companion object {
        const val TAG = "WritePostSheet"
    }
}