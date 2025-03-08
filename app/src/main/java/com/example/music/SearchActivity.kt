package com.example.music

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class SearchActivity : AppCompatActivity() {
    private lateinit var back: ImageButton
    private lateinit var searchBar: EditText
    private lateinit var item_cyc: RecyclerView
    private lateinit var adapter: SearchAdapter
    private var media: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        back = findViewById(R.id.back)
        searchBar = findViewById(R.id.searchBar)
        item_cyc = findViewById(R.id.item_recyc)


        item_cyc.layoutManager = LinearLayoutManager(this)


        searchBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val query = searchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchMusic(query) { trackList ->
                        runOnUiThread {
                            adapter = SearchAdapter(trackList) { Track ->
                                val intent = Intent(this, MusicPlayActivity::class.java).apply {
                                    putStringArrayListExtra("TRACK_LIST",ArrayList())
                                    putExtra("TRACK_NAME", Track.name)
                                    putExtra("TRACK_IMAGE", Track.image)
                                    putExtra("TRACK_AUDIO", Track.audio)
                                }
                                startActivity(intent)
                            }

                            item_cyc.adapter = adapter
                        }
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }

        back.setOnClickListener {
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        media?.release()
        media = null
    }
}