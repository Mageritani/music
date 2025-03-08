package com.example.music

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.load
import java.util.concurrent.TimeUnit

class MusicPlayActivity : AppCompatActivity() {
    // 使用by lazy进行懒加载初始化UI组件
    private val back by lazy { findViewById<ImageButton>(R.id.back) }
    private val play by lazy { findViewById<ImageButton>(R.id.play) }
    private val next by lazy { findViewById<ImageButton>(R.id.next) }
    private val previous by lazy { findViewById<ImageButton>(R.id.previous) }
    private val image by lazy { findViewById<ImageView>(R.id.image) }
    private val songTitle by lazy { findViewById<TextView>(R.id.song_title) }
    private val seekBar by lazy { findViewById<SeekBar>(R.id.seekBar) }
    private val currentTime by lazy { findViewById<TextView>(R.id.current_time) }
    private val totalTime by lazy { findViewById<TextView>(R.id.total_time) }

    private var songList = arrayListOf<String>()
    private var trackIndex = 0
    private var trackNameList = arrayListOf<String>()
    private var trackImageList = arrayListOf<String>()

    private var media: CustomMediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_play)
        // 获取Intent传递的数据
        with(intent) {
            songList = getStringArrayListExtra("TRACK_LIST") ?: arrayListOf()
            trackIndex = getIntExtra("TRACK_INDEX", 0)
            trackNameList = getStringArrayListExtra("TRACK_NAMES") ?: arrayListOf()
            trackImageList = getStringArrayListExtra("TRACK_IMAGES") ?: arrayListOf()

            // 处理单曲播放的情况
            val trackName = getStringExtra("TRACK_NAME")
            val trackImage = getStringExtra("TRACK_IMAGE")
            val trackAudio = getStringExtra("TRACK_AUDIO")

            // 如果没有列表但有单曲信息，添加到列表
            if (trackName != null && trackImage != null) {
                if (trackNameList.isEmpty()) trackNameList.add(trackName)
                if (trackImageList.isEmpty()) trackImageList.add(trackImage)
            }

            // 初始化UI
            updateUI(trackIndex)

            // 设置播放器
            setupMusicPlayer(songList, trackIndex, trackAudio)
        }

        setupClickListeners()
    }

    private fun setupMusicPlayer(songList: ArrayList<String>, trackIndex: Int, trackAudio: String?) {
        try {
            media = CustomMediaPlayer(this).apply {
                setOnPreparedListener {
                    val duration = getDuration()
                    seekBar.max = duration
                    totalTime.text = formatTime(duration.toLong())
                    updatePlayPauseButton()
                }

                setOnCompletionListener {
                    updatePlayPauseButton()
                }

                setOnErrorListener { errorMsg ->
                    Log.e("MusicPlayerActivity", errorMsg)
                }

                setOnProgressListener { current, total ->
                    seekBar.progress = current
                    currentTime.text = formatTime(current.toLong())
                }

                setOnTrackChangeListener { index, _ ->
                    runOnUiThread {
                        updateUI(index)
                        Log.d("customMedia", "已更新歌曲信息")
                    }
                }

                // 设置播放列表
                setPlayList(songList, trackIndex)

                // 如果有单独传入的音频链接，使用它
                trackAudio?.let {
                    Log.d("customMedia", "设置音源$it")
                    setDataSource(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        back.setOnClickListener { finish() }

        play.setOnClickListener {
            media?.playPause()
            updatePlayPauseButton()
        }

        next.setOnClickListener { media?.next() }

        previous.setOnClickListener { media?.previous() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    media?.seekTo(progress)
                    currentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updatePlayPauseButton() {
        val isPlaying = media?.isPlaying() ?: false
        play.setImageResource(
            if (isPlaying) R.drawable.baseline_pause_circle_24
            else R.drawable.baseline_play_circle_24
        )
    }

    private fun updateUI(index: Int) {
        if (index in trackNameList.indices) {
            songTitle.text = trackNameList[index]
            image.load(trackImageList[index]) {
                crossfade(true)
            }
            updatePlayPauseButton()
            trackIndex = index
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        media?.release()
        media = null
    }
}