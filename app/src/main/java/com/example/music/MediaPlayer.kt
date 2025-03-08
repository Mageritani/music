package com.example.music

import android.content.Context
import android.media.AudioAttributes
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

    // 使用高阶函数简化回调
    private var onPreparedListener: (() -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var onProgressListener: ((Int, Int) -> Unit)? = null
    private var onTrackChangeListener: ((Int, String) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnableInterval = 1000L

    private val progressRunnable = object : Runnable {
        override fun run() {
            media?.let {
                if (it.isPlaying) {
                    onProgressListener?.invoke(it.currentPosition, it.duration)
                    handler.postDelayed(this, progressRunnableInterval)
                }
            }
        }
    }

    // 简化的setter方法
    fun setOnProgressListener(listener: (Int, Int) -> Unit) { onProgressListener = listener }
    fun setOnPreparedListener(listener: () -> Unit) { onPreparedListener = listener }
    fun setOnCompletionListener(listener: () -> Unit) { onCompletionListener = listener }
    fun setOnErrorListener(listener: (String) -> Unit) { onErrorListener = listener }
    fun setOnTrackChangeListener(listener: (Int, String) -> Unit) { onTrackChangeListener = listener }

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
                    val errorMessage = "播放错误: what=$what extra=$extra"
                    Log.e("CustomMusicPlayer", errorMessage)
                    release()
                    onErrorListener?.invoke(errorMessage)
                    true
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            Log.e("CustomMusicPlayer", "设置音源时出错", e)
            onErrorListener?.invoke("设置音源时出错: ${e.message}")
        }
    }

    fun seekTo(position: Int) {
        if (isPrepared) {
            media?.seekTo(position)
            currentPosition = position
        } else {
            Log.e("customMedia", "播放器尚未准备好，无法跳转")
        }
    }

    private fun startProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    fun playPause() {
        if (!isPrepared) {
            Log.e("customMedia", "播放器尚未准备好")
            return
        }

        media?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    handler.removeCallbacks(progressRunnable)
                } else {
                    it.start()
                    startProgressUpdates()
                }
            } catch (e: IllegalStateException) {
                Log.e("customMedia", "播放/暂停时出错: ${e.message}")
            }
        }
    }

    // 简化next和previous方法
    fun next() {
        if (songList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % songList.size
            playSongAtIndex(currentIndex)
        }
    }

    fun previous() {
        if (songList.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
            playSongAtIndex(currentIndex)
        }
    }

    // 抽取公共逻辑到一个方法
    private fun playSongAtIndex(index: Int) {
        if (index < songList.size) {
            val song = songList[index]
            setDataSource(song)
            onTrackChangeListener?.invoke(index, song)
        }
    }

    fun setPlayList(songs: List<String>, startIndex: Int = 0) {
        songList = songs.toMutableList()
        currentIndex = startIndex.coerceIn(0, songs.size - 1)
        if (songList.isNotEmpty()) {
            playSongAtIndex(currentIndex)
        }
    }

    // 简化的getter方法
    fun isPlaying() = media?.isPlaying ?: false
    fun getDuration() = media?.duration ?: 0
    fun getCurrentPosition() = media?.currentPosition ?: 0

    fun release() {
        currentPosition = getCurrentPosition()
        handler.removeCallbacks(progressRunnable)
        isPrepared = false
        media?.release()
        media = null
    }
}