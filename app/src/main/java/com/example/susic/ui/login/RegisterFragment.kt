package com.example.susic.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.susic.MainActivity
import com.example.susic.R
import com.example.susic.data.Artist
import com.example.susic.data.User
import com.example.susic.databinding.FragmentRegistBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegistBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_regist, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = Firebase.auth
        db = Firebase.firestore
        binding.registerBtn.setOnClickListener {
            val mail = binding.mail.editText?.text.toString().trim()
            val pass = binding.password.editText?.text.toString()
            val fullName = binding.username.editText?.text.toString()
            if (mail != "" && pass != "" && fullName != "") {
                auth.createUserWithEmailAndPassword(
                    mail,
                    pass
                )
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            auth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener {

                                val splitIndex = fullName.indexOf(" ")
                                val firstName = fullName.substring(0, splitIndex)
                                val lastName = fullName.substring(splitIndex, fullName.length)
                                db.collection("users").add(
                                    User(
                                        id = auth.currentUser?.uid.toString(),
                                        contact = auth.currentUser?.email.toString(),
                                        firstname = firstName,
                                        lastname = lastName,
                                        birthday = Calendar.getInstance().time
                                    )
                                ).addOnCompleteListener {
                                    Log.i("Register info", "${it.result}")
                                }.addOnFailureListener {
                                    Log.i("Error:", it.toString())
                                }
                                val intent = Intent(context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        } else
                            Toast.makeText(context, "Invalid credential", Toast.LENGTH_LONG).show()
                    }
            } else {
                if (fullName == "") {
                    binding.username.error = getString(R.string.invalid_username)
                }
                if (mail == "") binding.mail.error = getString(R.string.invalid_mail)
                if (pass == "") binding.password.error = getString(R.string.invalid_password)
            }
        }
    }

    companion object {

    }
}