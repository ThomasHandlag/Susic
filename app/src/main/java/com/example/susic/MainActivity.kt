package com.example.susic

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.susic.data.Artist
import com.example.susic.data.Track
import com.example.susic.data.User
import com.example.susic.databinding.ActivityMainBinding
import com.example.susic.databinding.HeaderNavBinding
import com.example.susic.databinding.PlayerFloatBinding
import com.example.susic.network.DB
import com.example.susic.player.*
import com.example.susic.ui.home.HomeFragment
import com.example.susic.ui.artist.ArtistFragment
import com.example.susic.ui.library.LibraryFragment
import com.example.susic.ui.login.ServiceActivity
import com.example.susic.ui.notify.NotifyFragment
import com.example.susic.ui.profile.ProfileFragment
import com.example.susic.ui.profile.UserProfileFragment
import com.example.susic.ui.setting.SettingsFragment
import com.example.susic.ui.sheets.WritePostSheet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mMediaPlayerHolder get() = MediaPlayerHolder.getInstance()
    private lateinit var mPlayerService: PlayerService
    private var sBound = false
    private lateinit var mBindingIntent: Intent
    private lateinit var mPlayerControlsPanelBinding: PlayerFloatBinding
    private val viewModel: SusicViewModel by viewModels()

    private val aShowPlayer: (track: Track) -> Unit = {
        with(binding) {
            player.visibility = View.VISIBLE
            imgThumb.userImg(it.urlImage)
            trackTitle.text = getString(R.string.player_title, it.name)
            closeActionBtn.setOnClickListener {
                player.visibility = View.GONE
                mMediaPlayerHolder.pausePlayer()
                mMediaPlayerHolder.resetPlayer()
            }
            mMediaPlayerHolder.setupNotificationPlayer(it.url)
            prgIndicator.max = mMediaPlayerHolder.sPlayerDuration
            mMediaPlayerHolder.playPlayer()

            mMediaPlayerHolder.mediaPlayerInterface = mMediaPlayerInterface

            pauseBtn.setOnClickListener { _ ->
                when (mMediaPlayerHolder.notificationPlayerState) {
                    PlayerState.PAUSE -> {
                        mMediaPlayerHolder.playPlayer()
                    }

                    PlayerState.PLAYING -> {
                        mMediaPlayerHolder.pausePlayer()
                    }

                    else -> {
                        mMediaPlayerHolder.reset()
                        mMediaPlayerHolder.setupNotificationPlayer(it.url)
                        mMediaPlayerHolder.playPlayer()
                    }
                }
            }
            prgIndicator.postDelayed(object : Runnable {
                override fun run() {
                    prgIndicator.progress = mMediaPlayerHolder.currentPlayerPosition
                    prgIndicator.postDelayed(this, 100)
                }
            }, 10)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser != null) {
            binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
            val intent = Intent(this, PlayerService::class.java)
            val sheet = WritePostSheet()
            DB.listenChangedToNotify()
            val n: (i: Int) -> Unit = {
                binding.bottomNavigationView.getOrCreateBadge(R.id.notify_page).number = it
            }
            DB.countNotification(n)
            val viewDetail: (fragment: Fragment, user: User) -> Unit = { it, u ->
                viewModel.setCurrentViewedUser(u)
                switchFragment(fragment = it)
            }
            val showDetail: (fragment: Fragment) -> Unit = { fr ->
                switchFragment(fr)
            }

            viewModel.iGetCurrentUser()
            startService(intent)
            binding.bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.home_page -> {
                        binding.player.visibility = View.GONE
                        mMediaPlayerHolder.resetPlayer()
                        switchFragment(HomeFragment())
                    }

                    R.id.notify_page -> switchFragment(NotifyFragment())
                    R.id.settings_page -> switchFragment(SettingsFragment())
                    R.id.lib_page -> switchFragment(LibraryFragment(aShowPlayer, showDetail))
                    else -> {
                        viewModel.getViewUsers()
                        switchFragment(ArtistFragment(viewDetail))
                    }

                }
            }
            val drawerLayout = binding.drawerLayout
            binding.topAppBar.setNavigationOnClickListener {
                drawerLayout.open()
            }
            binding.navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.wp_btn -> {
                        sheet.show(supportFragmentManager, WritePostSheet.TAG)
                        drawerLayout.close()
                        menuItem.isChecked = true
                        true
                    }

                    R.id.prf_btn -> {
                        switchFragment(ProfileFragment())
                        binding.bottomNavigationView.clearFocus()
                        menuItem.isChecked = true
                        drawerLayout.close()
                        true
                    }

                    R.id.sl_btn -> {
                        menuItem.isChecked = true
                        drawerLayout.close()
                        true
                    }
                    R.id.view_prf_btn -> {
                        switchFragment(UserProfileFragment())
                        menuItem.isChecked = true
                        drawerLayout.close()
                        true
                    }
                    R.id.st_btn -> {
                        menuItem.isChecked = true
                        drawerLayout.close()
                        switchFragment(SettingsFragment())
                        true
                    }

                    R.id.child1 -> {
                        drawerLayout.close()
                        menuItem.isChecked = true
                        true
                    }

                    else -> {
                        drawerLayout.close()
                        menuItem.isChecked = true
                        true
                    }
                }
            }

            viewModel.headerState.observe(this) {
                when (it) {
                    StatusEnums.DONE -> {
                        val navBinding: HeaderNavBinding = DataBindingUtil.inflate(
                            layoutInflater,
                            R.layout.header_nav,
                            binding.navigationView,
                            false
                        )
                        binding.navigationView.addHeaderView(navBinding.root)
                        navBinding.viewModel = viewModel
                    }

                    else -> {}
                }
            }
        } else {
            val i = Intent(this, ServiceActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            this.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        doBindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(mBindingIntent)
        if (sBound) unbindService(connection)
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

    private val mMediaPlayerInterface = object : MediaPlayerInterface {
        override fun onClose() {
            finishAndRemoveTask()
        }

        override fun onPositionChanged(position: Int) {
            mPlayerControlsPanelBinding.prgIndicator
        }

        override fun onStateChanged() {

        }


        override fun onRepeat(toastMessage: Int) {

        }

        override fun onStart() {
            binding.pauseBtn.setIconResource(R.drawable.ic_round_pause_circle_24)
        }

        override fun onPause() {
            binding.pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
        }

        override fun onComplete() {
            binding.pauseBtn.setIconResource(R.drawable.ic_round_play_arrow_24)
        }
    }
}

class MusicNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {

    }

}