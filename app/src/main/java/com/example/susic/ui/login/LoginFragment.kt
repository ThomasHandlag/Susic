package com.example.susic.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.susic.MainActivity
import com.example.susic.R
import com.example.susic.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = Firebase.auth
        binding.login.setOnClickListener {
            val mail = binding.username.editText?.text.toString().trim()
            val pass = binding.password.editText?.text.toString().trim()
            auth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    runMainActivity()
                } else Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun runMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {

    }
}