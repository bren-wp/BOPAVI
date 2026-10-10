package com.brendigo.bopavi

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/** Fully offline, original audio. Sound effects are preloaded and loops are released on exit. */
class Soundscape(private val context: Context) {
    private val pool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val effects = mapOf(
        "tap" to pool.load(context,R.raw.tap,1),"collect" to pool.load(context,R.raw.collect,1),
        "level" to pool.load(context,R.raw.level,1),"hit" to pool.load(context,R.raw.hit,1),
        "purchase" to pool.load(context,R.raw.purchase,1),"click" to pool.load(context,R.raw.click,1))
    private val worlds = intArrayOf(R.raw.world_0,R.raw.world_1,R.raw.world_2,R.raw.world_3,
        R.raw.world_4,R.raw.world_5,R.raw.world_6,R.raw.world_7)
    private var player: MediaPlayer? = null
    var musicVolume:Float=0.80f
        set(value){
            field=value.coerceIn(0f,1f)
            try {player?.setVolume(.25f*field,.25f*field)}catch(_:Exception){}
        }
    var effectsVolume:Float=0.70f
        set(value){field=value.coerceIn(0f,1f)}
    var enabled = true
        set(value){field=value; if(!value)pause() else resume()}
    private var world = -1
    fun startWorld(index:Int) {
        if(index !in 0..7)return
        if(world==index && player!=null){resume();return}
        stop(); world=index
        try {
            player=MediaPlayer.create(context,worlds[index])?.apply {
                isLooping=true;setVolume(.25f*musicVolume,.25f*musicVolume);if(enabled)start()
            }
        }catch(_:Exception){stop()}
    }
    fun effect(name:String) {
        if(enabled && effectsVolume>0f)effects[name]?.let {
            pool.play(it,.44f*effectsVolume,.44f*effectsVolume,1,0,1f)
        }
    }
    fun pause(){try {if(player?.isPlaying==true)player?.pause()}catch(_:Exception){}}
    fun resume(){try {if(enabled && player?.isPlaying==false)player?.start()}catch(_:Exception){}}
    fun stop(){try{player?.release()}catch(_:Exception){};player=null;world=-1}
    fun close(){stop();pool.release()}
}
