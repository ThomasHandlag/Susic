package com.example.susic.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.example.susic.R
import com.example.susic.databinding.CommentSectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentSheet(private val manager: FragmentManager, private val tagVal: String) :
    BottomSheetDialogFragment() {
    private lateinit var commentSheet: CommentSectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        commentSheet =
            DataBindingUtil.inflate(layoutInflater, R.layout.comment_section, container, false)
        return commentSheet.root
    }

    companion object {
        const val TAG = "CommentSheet"
    }

    fun initView() {

    }

    fun show() {
        super.show(manager, tag)
    }
}