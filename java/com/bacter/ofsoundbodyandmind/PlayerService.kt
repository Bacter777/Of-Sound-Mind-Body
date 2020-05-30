package com.bacter.ofsoundbodyandmind

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlayerService : Service() {
    private val notificationID = 132
    private val tag = "player"

    // called when sound is started or stopped
    var playChangeListener:(()-> Unit) ?=null

    inner class Playerbinder:Binder(){
        fun getService(): PlayerService{
            return this@PlayerService
        }
    }
    private val playerBinder=Playerbinder()
    override fun onCreate() {
        Sound.values().forEach{
            exoPlayers[it]=initializeExoPlayer(it.file)
        }
    }
    enum class Sound(val file: String){
        ELEVEN ("elevenforest.mp3"),
        GREEN ("greennature.mp3"),
        HEALING ("healingwater.mp3"),
        QUIET ("thequietmorning.mp3"),
        TRANQUILITY("tranquility.mp3"),
        FRIENDS("wewerefriends.mp3"),
        DEEP("deepmeditation.mp3"),
        MOMENT("inthemoment.mp3"),
        LOVE("lovespell.mp3")
    }
    private val exoPlayers= mutableMapOf<Sound,SimpleExoPlayer>()
    private fun initializeExoPlayer(soundFile:String):SimpleExoPlayer{
        val exoPlayer=ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),DefaultTrackSelector()
        )
        val dataSource = DefaultDataSourceFactory(this,
            Util.getUserAgent(this, this.getString(R.string.app_name)))
        val mediaSource = ExtractorMediaSource.Factory(dataSource)
            .createMediaSource(Uri.parse("asset:///$soundFile"))
        Log.d("MAIN","loading $soundFile")
        exoPlayer.prepare(mediaSource)
        exoPlayer.repeatMode=Player.REPEAT_MODE_ALL
        return exoPlayer
    }

    override fun onUnbind(intent: Intent?): Boolean {
        playChangeListener=null
        if (!isPlaying()){
            stopSelf()
            Log.d(tag,"stopping service")
        }
        return super.onUnbind(intent)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return playerBinder
    }
    fun startForeground(){
        if (SDK_INT>=24 && isPlaying()){
            val notificationIntent= Intent(this,MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent,0)
            val notification = NotificationCompat.Builder(this,"ofSound")
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_volume)
                .setContentIntent(pendingIntent)
                .build()
            Log.d(tag, "starting foreground service...")
            startForeground(notificationID,notification)
        }
    }
    fun stopForeground(){
        if (SDK_INT>=24){
            Log.d(tag,"stopping foreground service...")
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }
    fun stopPlaying(){
        exoPlayers.values.forEach { it.playWhenReady = false }
    }
    fun isPlaying(): Boolean{
        var playing = false
        exoPlayers.values.forEach { if (it.playWhenReady)playing=true }
        return playing
    }
    fun setVolume(sound: Sound, volume: Float){
        exoPlayers[sound]?.playWhenReady=!(exoPlayers[sound]?.playWhenReady?: false)
        playChangeListener?.invoke()
    }
    fun toggleSound(sound: Sound){
        exoPlayers[sound]?.playWhenReady=!(exoPlayers[sound]?.playWhenReady ?:false)
        playChangeListener?.invoke()
    }
}