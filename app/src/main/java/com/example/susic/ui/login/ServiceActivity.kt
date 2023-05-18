package com.example.susic.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.susic.MainActivity

import com.example.susic.R
import com.example.susic.databinding.ActivityServiceBinding
import com.example.susic.ui.artist.ArtistFragment
import com.example.susic.ui.home.HomeFragment
import com.example.susic.ui.library.LibraryFragment
import com.example.susic.ui.notify.NotifyFragment
import com.example.susic.ui.setting.SettingsFragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ServiceActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityServiceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            signSucceed()
        }
        val sharePref = getSharedPreferences("THEME_STATE", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(sharePref.getInt("THEME_MODE", 0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service)
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.sign_in -> switchFragment(LoginFragment())
                else -> switchFragment(RegisterFragment())
            }
        }
    }
    private fun switchFragment(fragment: Fragment) : Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.service_nav_host_fragment, fragment)
        transaction.commit()
        return true
    }
    private fun signSucceed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.finish()
    }
}
//        val username = binding.username
//        val password = binding.password
//        val login = binding.login
//        val loading = binding.loading
//
//        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]
//
//        loginViewModel.loginFormState.observe(this@ServiceActivity, Observer {
//            val loginState = it ?: return@Observer
//
//            // disable login button unless both username / password is valid
//            login.isEnabled = loginState.isDataValid
//
//            if (loginState.usernameError != null) {
//                username.error = getString(loginState.usernameError)
//            }
//            if (loginState.passwordError != null) {
//                password.error = getString(loginState.passwordError)
//            }
//        })
//
//        loginViewModel.loginResult.observe(this@ServiceActivity, Observer {
//            val loginResult = it ?: return@Observer
//
//            loading.visibility = View.GONE
//            if (loginResult.error != null) {
//                showLoginFailed(loginResult.error)
//            }
//            if (loginResult.success != null) {
//                updateUiWithUser(loginResult.success)
//            }
//            setResult(Activity.RESULT_OK)
//
//            //Complete and destroy login activity once successful
//            finish()
//        })
//
//        username.editText?.afterTextChanged {
//            loginViewModel.loginDataChanged(
//                username.editText?.text.toString(),
//                password.editText?.text.toString()
//            )
//        }
//
//        password.editText?.apply {
//            afterTextChanged {
//                loginViewModel.loginDataChanged(
//                    username.editText?.text.toString(),
//                    password.editText?.text.toString()
//                )
//            }
//
//            setOnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE ->
//                        loginViewModel.login(
//                            username.editText?.text.toString(),
//                            password.editText?.text.toString()
//                        )
//                }
//                false
//            }
//
//            login.setOnClickListener {
//                loading.visibility = View.VISIBLE
//                loginViewModel.login(username.editText?.text.toString(), password.editText?.text.toString())
//            }
//        }
//    }
//
//    private fun updateUiWithUser(model: LoggedInUserView) {
//        val welcome = getString(R.string.welcome)
//        val displayName = model.displayName
//        // TODO : initiate successful logged in experience
//        Toast.makeText(
//            applicationContext,
//            "$welcome $displayName",
//            Toast.LENGTH_LONG
//        ).show()
//    }
//
//    private fun showLoginFailed(@StringRes errorString: Int) {
//        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
//    }
//
//}
//
///**
// * Extension function to simplify setting an afterTextChanged action to EditText components.
// */
//fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
//    this.addTextChangedListener(object : TextWatcher {
//        override fun afterTextChanged(editable: Editable?) {
//            afterTextChanged.invoke(editable.toString())
//        }
//
//        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//
//        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//    })