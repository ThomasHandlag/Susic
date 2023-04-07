package com.example.susic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.susic.databinding.ActivityMainBinding
import com.example.susic.databinding.FragmentHomeBinding
import com.example.susic.databinding.PlayerFloatBinding
import com.example.susic.databinding.PostItemBinding
import com.example.susic.player.*
import com.example.susic.ui.home.HomeFragment
import com.example.susic.ui.artist.ArtistFragment
import com.example.susic.ui.library.LibraryFragment
import com.example.susic.ui.notify.NotifyFragment
import com.example.susic.ui.setting.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var playerService: PlayerService
    private var mPlayer: MediaPlayerUI? = null
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private lateinit var mPlayerService: PlayerService
    private var sBound = false
    private lateinit var mBindingIntent: Intent
    private lateinit var mPlayerControlsPanelBinding: PlayerFloatBinding
    private lateinit var sPostItemBinding: PostItemBinding
    private lateinit var sHomeBinding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        val intent = Intent(this, PlayerService::class.java)
        startForegroundService(intent)
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_page -> switchFragment(HomeFragment().apply { })
                R.id.notify_page -> switchFragment(NotifyFragment().apply { })
                R.id.settings_page -> switchFragment(SettingsFragment().apply { })
                R.id.lib_page -> switchFragment(LibraryFragment().apply { })
                else -> switchFragment(ArtistFragment().apply { })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        doBindService()
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.commit()
        return true
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            // get bound service and instantiate MediaPlayerHolder
            val binder = service as PlayerService.LocalBinder
            mPlayerService = binder.getService()
            sBound = true

            mMediaPlayerHolder.mediaPlayerInterface = mMediaPlayerInterface
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            sBound = false
        }
    }

    private fun doBindService() {
        mBindingIntent = Intent(this, PlayerService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    //    override fun onResumeFragments() {
//        super.onResumeFragments()
//        //TODO
//    }
    private val mMediaPlayerInterface = object : MediaPlayerInterface {
        override fun onClose() {
            //finish activity if visible
            finishAndRemoveTask()
        }

        override fun onPositionChanged(position: Int) {
            mPlayerControlsPanelBinding.prgIndicator.setProgressCompat(position, true)
        }

        override fun onStateChanged() {

        }


        override fun onRepeat(toastMessage: Int) {

        }

        override fun onStart() {
            mPlayerControlsPanelBinding.pauseBtn.setIconResource(R.drawable.ic_round_pause_circle_24)
        }

        override fun onPause() {
            mPlayerControlsPanelBinding.pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
        }
    }
}
