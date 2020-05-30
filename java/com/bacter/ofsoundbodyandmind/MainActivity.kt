package com.bacter.ofsoundbodyandmind

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var playerService: PlayerService? = null
    private val serviceConnection=object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService=(service as PlayerService.Playerbinder).getService()


            if (playerService?.isPlaying()== true) fab.show() else fab.hide()
            playerService?.playChangeListener=playerChangeListener
        }
    }
    private val playerChangeListener={
        if (playerService?.isPlaying()==true)fab.show()else fab.hide()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        createNotificationChannel()
        play_rain.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.ELEVEN)
            toggleProgressBar(rain_volume)
        }
        play_storm.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.GREEN)
            toggleProgressBar(storm_volume)
        }
        play_water.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.HEALING)
            toggleProgressBar(water_volume)
        }
        play_fire.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.QUIET)
            toggleProgressBar(fire_volume)
        }
        play_wind.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.TRANQUILITY)
            toggleProgressBar(wind_volume)
        }
        play_night.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.FRIENDS)
            toggleProgressBar(night_volume)
        }
        play_deep.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.DEEP)
            toggleProgressBar(deep_volume)
        }
        play_moment.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.MOMENT)
            toggleProgressBar(moment_volume)
        }
        play_love.setOnClickListener {
            playerService?.toggleSound(PlayerService.Sound.LOVE)
            toggleProgressBar(love_volume)
        }
        rain_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.ELEVEN))
        storm_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.GREEN))
        water_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.HEALING))
        fire_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.QUIET))
        wind_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.TRANQUILITY))
        night_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.FRIENDS))
        deep_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.DEEP))
        moment_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.MOMENT))
        love_volume.setOnSeekBarChangeListener(VolumeChangeListener(PlayerService.Sound.LOVE))

        fab.setOnClickListener{
            playerService?.stopPlaying()
            fab.hide()
            arrayOf(rain_volume,storm_volume,water_volume,fire_volume,wind_volume,night_volume).forEach { bar ->
                bar.visibility= View.INVISIBLE
            }
        }
    }
    private fun toggleProgressBar(progressBar: ProgressBar){
        progressBar.visibility=if (progressBar.visibility==View.VISIBLE)View.INVISIBLE else View.VISIBLE
    }
    inner class VolumeChangeListener(private val sound: PlayerService.Sound) : SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            playerService?.setVolume(sound,(progress +1) /20f)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
    override fun onStart(){
        super.onStart()
        val playerIntent=Intent(this, PlayerService::class.java)
        startService(playerIntent)
        bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        playerService?.stopForeground()
    }

    override fun onPause() {
        playerService?.startForeground()
        super.onPause()
    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name=getString(R.string.app_name)
            val importance=NotificationManager.IMPORTANCE_MIN
            val channel=NotificationChannel("ofSound",name,importance)

            val notificationManager=getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onBackPressed() {
       val builder=android.app.AlertDialog.Builder(this)
       builder.setIcon(R.mipmap.ic_launcher_round)
       builder.setCancelable(false)
       builder.setTitle("Bacter Says")
       builder.setMessage("Are you sure you want to exit?")
       builder.setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
           finish()
       }
       builder.setNegativeButton("No"){ dialog: DialogInterface?, which: Int ->
       }
       builder.create()
       builder.show()
   }
}
