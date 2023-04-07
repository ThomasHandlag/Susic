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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRegistBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

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
            auth.createUserWithEmailAndPassword(
                mail,
                pass
            )
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        auth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener {
                            val fullName = binding.username.editText?.text.toString()
                            val splitIndex = fullName.indexOf(" ")
                            val firstName = fullName.substring(0, splitIndex)
                            val lastName = fullName.substring(splitIndex, fullName.lastIndex)
                            db.collection("users").add(
                                User(
                                    id = auth.currentUser?.uid.toString(),
                                    contact = auth.currentUser?.email.toString(),
                                    firstName = firstName,
                                    lastName = lastName
                                )
                            ).addOnCompleteListener {
                                Log.i("Register info", "${it.result}")
                            }.addOnFailureListener {
                                Log.i("Error:", "Add document failed")
                            }
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    } else
                        Toast.makeText(context, "Invalid credential", Toast.LENGTH_LONG).show()
                }
        }
        binding.login.setOnClickListener {
            this.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}