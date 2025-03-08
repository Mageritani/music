package com.example.music

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.load
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class MusicPlayActivity : AppCompatActivity() {
    private lateinit var back: ImageButton
    private lateinit var play: ImageButton
    private lateinit var next: ImageButton
    private lateinit var previous: ImageButton
    private lateinit var image: ImageView
    private lateinit var song_title: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var current_time: TextView
    private lateinit var total_time: TextView
    private lateinit var linearLayout: LinearLayout

    private lateinit var songList : ArrayList<String>
    private var trackIndex = 0

    private  var media: CustomMediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_play)

        back = findViewById(R.id.back)
        play = findViewById(R.id.play)
        next = findViewById(R.id.next)
        previous = findViewById(R.id.previous)
        image = findViewById(R.id.image)
        song_title = findViewById(R.id.song_title)
        seekBar = findViewById(R.id.seekBar)
        current_time = findViewById(R.id.current_time)
        total_time = findViewById(R.id.total_time)
        linearLayout = findViewById(R.id.linearLayout)

        val trackName = intent.getStringExtra("TRACK_NAME")
        val trackImage = intent.getStringExtra("TRACK_IMAGE")
        val trackAudio = intent.getStringExtra("TRACK_AUDIO")


        val songList = intent.getStringArrayListExtra("TRACK_LIST") ?: arrayListOf()
        val trackIndex = intent.getIntExtra("TRACK_INDEX",0)

        song_title.text = trackName
        image.load(trackImage) {
            crossfade(true)
        }
        setupMusicPlayer(songList,trackIndex,trackAudio)
        setupClickListeners()
    }

    private fun setupMusicPlayer(songList : ArrayList<String>, trackIndex : Int,trackAudio: String?) {
        try {
            media = CustomMediaPlayer(this).apply {
                setOnPreparedListener {
                    Log.d("customMedia", "播放器準備完成，可以播放")
                    val duration = getDuration()
                    seekBar.max = duration
                    total_time.text = formatTime(duration.toLong())
                    updatePlayPauseButton() // 更新播放按钮状态
                }
                setOnCompletionListener {
                    updatePlayPauseButton() // 播放完成时更新按钮状态
                }
                setOnErrorListener { errorMsg ->
                    Log.e("MusicPlayerActivity", errorMsg)
                }
                setOnProgressListener { current, total ->
                    seekBar.progress = current
                    current_time.text = formatTime(current.toLong())
                }
                trackAudio?.let {
                    Log.d("customMedia", "設定音源$it")
                    setDataSource(it)
                }
                setPlayList(songList,trackIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        back.setOnClickListener {
            finish()
        }

        play.setOnClickListener {
            media?.playPause()
            // 添加短暂延迟来确保状态已更新
            handler.postDelayed({
                updatePlayPauseButton()
            }, 100)
        }

        next.setOnClickListener {
            media?.next()
        }

        previous.setOnClickListener {
            media?.previous()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    media?.seekTo(progress)
                    current_time.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updatePlayPauseButton() {
        val isPlaying = media?.isPlaying() ?: false
        Log.d("MusicPlayActivity", "更新按钮状态: isPlaying=${isPlaying}")

        if (isPlaying) {
            play.setImageResource(R.drawable.baseline_pause_circle_24)
        } else {
            play.setImageResource(R.drawable.baseline_play_circle_24)
        }
    }

    private fun formatTime(toLong: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(toLong)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(toLong) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        media?.release()
        media = null
    }
}