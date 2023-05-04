package com.example.susic.ui.notify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.susic.R
import com.example.susic.StatusEnums
import com.example.susic.SusicViewModel
import com.example.susic.databinding.FragmentNotifyBinding


class NotifyFragment : Fragment() {
    private lateinit var bind: FragmentNotifyBinding

    private val viewModel: SusicViewModel by lazy {
        ViewModelProvider(requireActivity())[SusicViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("NotifyFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container, false)
        val choice: (id: String, ch: Boolean) -> Unit = { it, ch ->
            if (ch) viewModel.accept(it) else viewModel.reject(it)
        }
        val adapter = NotifyAdapter(choice)
        bind.recView.adapter = adapter
        viewModel.notification.observe(requireActivity()) {
            adapter.submitList(it)
        }
        viewModel.notifyState.observe(requireActivity()) {
            with(bind) {
                when (it) {
                    StatusEnums.DONE -> {
                        recView.visibility = View.VISIBLE
                        img.visibility = View.GONE
                        prgBar.visibility =View.GONE
                    }
                    StatusEnums.LOADING -> {
                        recView.visibility = View.GONE
                        img.visibility = View.GONE
                        prgBar.visibility = View.VISIBLE
                    }
                    else -> {
                        recView.visibility = View.GONE
                        prgBar.visibility = View.GONE
                        img.visibility = View.VISIBLE
                    }
                }
            }
        }
        // Inflate the layout for this fragment
        return bind.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("NotifyFragment", "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Log.i("NotifyFragment", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("NotifyFragment", "onPause")
    }

    companion object {
    }
}