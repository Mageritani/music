package com.example.music

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {
    // 使用by lazy懒加载UI组件
    private val back by lazy { findViewById<ImageButton>(R.id.back) }
    private val searchBar by lazy { findViewById<EditText>(R.id.searchBar) }
    private val itemRecycler by lazy { findViewById<RecyclerView>(R.id.item_recyc) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        // 设置RecyclerView
        itemRecycler.layoutManager = LinearLayoutManager(this)
        // 设置搜索监听
        setupSearchListener()
        // 返回按钮
        back.setOnClickListener { finish() }
    }

    private fun setupSearchListener() {
        searchBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val query = searchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun performSearch(query: String) {
        searchMusic(query) { trackList ->
            runOnUiThread {
                val adapter = SearchAdapter(trackList) { selectedTrack ->
                    launchMusicPlayer(trackList, selectedTrack)
                }
                itemRecycler.adapter = adapter
            }
        }
    }

    private fun launchMusicPlayer(trackList: List<Track>, selectedTrack: Track) {
        val songUrls = trackList.map { it.audio }
        val songNames = trackList.map { it.name }
        val songImages = trackList.map { it.image }
        val selectedIndex = trackList.indexOf(selectedTrack)

        Intent(this, MusicPlayActivity::class.java).apply {
            putStringArrayListExtra("TRACK_LIST", ArrayList(songUrls))
            putStringArrayListExtra("TRACK_NAMES", ArrayList(songNames))
            putStringArrayListExtra("TRACK_IMAGES", ArrayList(songImages))
            putExtra("TRACK_INDEX", selectedIndex)
            putExtra("TRACK_NAME", selectedTrack.name)
            putExtra("TRACK_IMAGE", selectedTrack.image)
            putExtra("TRACK_AUDIO", selectedTrack.audio)
            startActivity(this)
        }
    }
}