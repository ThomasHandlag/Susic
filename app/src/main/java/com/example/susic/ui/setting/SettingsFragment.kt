package com.example.susic.ui.setting

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.susic.R
import com.example.susic.ui.login.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var auth: FirebaseAuth
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val themePreference: SwitchPreference? = findPreference("theme")
        val sharedPreferences =
            this.requireActivity().getSharedPreferences("THEME_STATE", MODE_PRIVATE)
        val sharePrefEdit: SharedPreferences.Editor = sharedPreferences.edit()
        val signOutPreference: Preference? = findPreference("signOut")
        auth = Firebase.auth
        signOutPreference?.setOnPreferenceClickListener {
            auth.signOut()
            val intent = Intent(this.requireActivity(), ServiceActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this.requireActivity().finish()
            true
        }
        themePreference?.setOnPreferenceChangeListener { _, _ ->
            if (themePreference.isChecked) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                themePreference.setIcon(R.drawable.ic_round_mode_night_24)
                sharePrefEdit.putInt("THEME_MODE", MODE_NIGHT_NO)
                sharePrefEdit.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                themePreference.setIcon(R.drawable.ic_round_light_mode_24)
                sharePrefEdit.putInt("THEME_MODE", MODE_NIGHT_YES)
                sharePrefEdit.apply()
            }
            true
        }
    }
}