package com.brendigo.bopavi

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

/** Offline original audio. Owns/releases focus, player and samples for one Activity. */
class Soundscape(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val musicAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val pool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val effects = mapOf(
        "tap" to pool.load(appContext,R.raw.tap,1),
        "collect" to pool.load(appContext,R.raw.collect,1),
        "level" to pool.load(appContext,R.raw.level,1),
        "hit" to pool.load(appContext,R.raw.hit,1),
        "click" to pool.load(appContext,R.raw.click,1))
    private val worlds = intArrayOf(R.raw.world_0,R.raw.world_1,R.raw.world_2,R.raw.world_3,
        R.raw.world_4,R.raw.world_5,R.raw.world_6,R.raw.world_7)

    private var player: MediaPlayer? = null
    private var closed = false
    private var focusHeld = false
    // A delayed or transiently lost request must still be explicitly abandoned
    // when the Activity pauses, even though it does not currently own focus.
    private var focusRequested = false
    private var playbackRequested = false
    private var world = -1

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                focusHeld = true
                if (!closed && enabled && playbackRequested) startPlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                focusHeld = false
                playbackRequested = false // Never restart after a permanent interruption.
                pausePlayer()
                abandonFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                focusHeld = false
                pausePlayer() // Resume only after focus returns and the game still wants music.
            }
        }
    }
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(musicAttributes)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(focusListener)
        .build()

    var musicVolume: Float = 0.80f
        set(value) {
            field = value.coerceIn(0f,1f)
            try {
                player?.setVolume(.25f*field,.25f*field)
            } catch (e: IllegalStateException) {
                Log.w("BOPAVI-Audio","Audio volume update skipped during player teardown",e)
            }
        }
    var effectsVolume: Float = 0.70f
        set(value) { field=value.coerceIn(0f,1f) }
    var enabled = true
        set(value) {
            field=value
            if (!value) pause() else resume()
        }

    private fun acquireFocus(): Boolean {
        if (closed || !enabled) return false
        if (focusRequested) return focusHeld
        val status = audioManager.requestAudioFocus(focusRequest)
        focusHeld = status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        focusRequested = focusHeld || status == AudioManager.AUDIOFOCUS_REQUEST_DELAYED
        return focusHeld
    }
    private fun abandonFocus() {
        if (focusRequested) {
            focusRequested = false
            focusHeld = false
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }
    private fun startPlayback() {
        try {
            val current=player
            if (current!=null && !current.isPlaying) current.start()
        } catch (e: IllegalStateException) {
            Log.w("BOPAVI-Audio","Unable to resume world music",e)
        }
    }
    private fun pausePlayer() {
        try {
            val current=player
            if (current?.isPlaying==true) current.pause()
        } catch (e: IllegalStateException) {
            Log.w("BOPAVI-Audio","World music already stopped",e)
        }
    }
    fun startWorld(index: Int) {
        if (closed || index !in worlds.indices) return
        if (world==index && player!=null) { resume(); return }
        stop()
        world=index
        try {
            player=MediaPlayer.create(appContext,worlds[index])?.apply {
                isLooping=true
                setVolume(.25f*musicVolume,.25f*musicVolume)
            }
        } catch (e: RuntimeException) {
            Log.w("BOPAVI-Audio","Unable to initialize local world music",e)
            stop()
            return
        }
        if (player==null) {
            Log.w("BOPAVI-Audio","Missing decodable local soundtrack")
            stop()
            return
        }
        resume()
    }
    fun effect(name: String) {
        if(closed || !enabled || effectsVolume<=0f)return
        effects[name]?.let { sample ->
            try {
                pool.play(sample,.44f*effectsVolume,.44f*effectsVolume,1,0,1f)
            } catch (e: IllegalStateException) {
                Log.w("BOPAVI-Audio","Sample playback skipped after audio teardown",e)
            }
        }
    }
    fun pause() {
        playbackRequested=false
        pausePlayer()
        abandonFocus()
    }
    fun resume() {
        if (closed || !enabled || player==null) return
        playbackRequested=true
        if (acquireFocus()) startPlayback()
    }
    fun stop() {
        playbackRequested=false
        pausePlayer()
        try {
            player?.release()
        } catch (e: IllegalStateException) {
            Log.w("BOPAVI-Audio","Audio release during teardown",e)
        }
        player=null
        world=-1
        abandonFocus()
    }
    fun close() {
        if(closed)return
        closed=true
        stop()
        pool.release()
    }
}
