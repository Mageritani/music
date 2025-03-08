package com.example.music

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException

class CustomMediaPlayer(private val context: Context) {

    private var media: MediaPlayer? = null
    private var isPrepared = false
    private var currentPosition = 0
    private var currentIndex = 0
    private var songList = mutableListOf<String>()
    private var onPreparedListener: (() -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var onProgressListener: ((Int, Int) -> Unit)? = null
    private var onTrackChangeListener: ((Int,String) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnableInterval = 1000L

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (isPlaying() && media != null) {
                val current = media?.currentPosition ?: 0
                val total = media?.duration ?: 0

                onProgressListener?.invoke(current, total)
                handler.postDelayed(this, progressRunnableInterval)
            }
        }
    }

    fun setOnProgressListener(listener: (Int, Int) -> Unit) {
        onProgressListener = listener
    }

    fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }

    fun setOnTrackChangeListener(listener: (Int,String) -> Unit){
        onTrackChangeListener = listener
    }

    fun setDataSource(audioUrl: String) {
        release()

        try {
            media = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)

                setOnPreparedListener {
                    isPrepared = true
                    onPreparedListener?.invoke()

                    if (currentPosition > 0) seekTo(currentPosition)
                }

                setOnCompletionListener {
                    onCompletionListener?.invoke()
                    handler.removeCallbacks(progressRunnable)
                }

                setOnErrorListener { _, what, extra ->
                    isPrepared = false
                    val errorMessage = "播放錯誤: what=$what  extra=$extra"
                    Log.e("CustomMusicPlayer", errorMessage)
                    release()
                    onErrorListener?.invoke(errorMessage)
                    true
                }
                prepareAsync()

            }
        } catch (e: IOException) {
            Log.e("CustomMusicPlayer", "設置音源時出錯", e)
            onErrorListener?.invoke("設置音源時出錯: ${e.message}")
        }
    }

    fun seekTo(position: Int) {
        if (!isPrepared) {
            Log.e("customMedia", "播放器尚未準備好，無法轉跳")
            return
        }
        media?.let {
            it.seekTo(position)
            currentPosition = position
        }
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    fun playPause() {
        if (!isPrepared) {
            Log.e("customMedia", "播放器尚未準備好")
            return
        }

        media?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    handler.removeCallbacks(progressRunnable)
                    Log.d("customMedia", "已暫停播放")
                } else {
                    it.start()
                    startProgressUpdates()
                    Log.d("customMedia", "已開始播放")
                }
            } catch (e: IllegalStateException) {
                Log.e("customMedia", "播放/暫停時出錯: ${e.message}")
            }
        } ?: Log.e("customMedia", "MediaPlayer 為空")

    }

    fun next(){
        if(songList.isNotEmpty()){
            currentIndex = (currentIndex+1)% songList.size
            val songName = songList[currentIndex]
            setDataSource(songName)

            onTrackChangeListener?.invoke(currentIndex,songName)
        }

    }

    fun previous(){
        if (songList.isNotEmpty()){
            currentIndex =
                if (currentIndex -1 < 0) {
                songList.size -1
            }else{
                currentIndex -1
            }
            val preSong = songList[currentIndex]
            setDataSource(preSong)

            onTrackChangeListener?.invoke(currentIndex,preSong)
        }
    }

    fun setPlayList(songs : List<String>,startIndex : Int = 0){
        songList = songs.toMutableList()
        currentIndex = startIndex
        if (songList.isNotEmpty() && currentIndex < songList.size){
            val currentSong = songList[currentIndex]
            setDataSource(currentSong)
            onTrackChangeListener?.invoke(currentIndex,currentSong)
        }
    }


    fun isPlaying(): Boolean {
        return media?.isPlaying ?: false
    }

    fun getDuration(): Int {
        return media?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return media?.currentPosition ?: 0
    }

    fun release() {
        currentPosition = getCurrentPosition()
        handler.removeCallbacks(progressRunnable)
        isPrepared = false
        media?.release()
        media = null
    }
}