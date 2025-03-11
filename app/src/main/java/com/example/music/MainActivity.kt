package com.example.music

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var title : TextView
    private lateinit var search : ImageButton
    private lateinit var notice : ImageButton
    private lateinit var recycler : RecyclerView

    private lateinit var adapter : OfflineTrackAdapter
    private lateinit var downloadManger: DownloadManger
    private var offlineTracks = listOf<OfflineTrack>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        downloadManger = DownloadManger(this)

        title = findViewById(R.id.title)
        search = findViewById(R.id.search)
        notice = findViewById(R.id.notice)
        recycler = findViewById(R.id.recycler)

        adapter = OfflineTrackAdapter(
            track = emptyList(),
            onTrackClick = {track,position ->
                plaOfflineTrack(track,position)
            },
            onTrackDelete = {track ->
                deleteOfflineTrack(track)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        loadOfflineTracks()

        search.setOnClickListener {
            val intent = Intent(this,SearchActivity::class.java)
            startActivity(intent)
        }

        notice.setOnClickListener {
            val intent = Intent(this,NoticeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun plaOfflineTrack(track: OfflineTrack, position: Int) {
        val trackNameList = offlineTracks.map { it.name } as ArrayList<String>
        val trackImageList = offlineTracks.map{it.image} as ArrayList<String>
        val trackAudioList = offlineTracks.map { it.localPath } as ArrayList<String>

        val intent = Intent(this,MusicPlayActivity::class.java).apply {
            putExtra("TRACK_LIST",trackAudioList)
            putExtra("TRACK_INDEX",position)
            putExtra("TRACK_NAME",trackNameList)
            putExtra("TRACK_IMAGE",trackImageList)
        }
        startActivity(intent)

    }

    private fun deleteOfflineTrack(track: OfflineTrack) {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO){
                downloadManger.deleteOfflineTrack(track)
            }
            if (success){
                loadOfflineTracks()
            }
        }
    }

    private fun loadOfflineTracks() {
        lifecycleScope.launch {
            val tracks  = withContext(Dispatchers.IO){
                downloadManger.getAllOfflineTracks()
            }
            offlineTracks = tracks
            adapter.updateTracks(tracks)
        }
    }


    override fun onResume() {
        super.onResume()
        loadOfflineTracks()
    }
}