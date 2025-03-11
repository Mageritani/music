package com.example.music

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.collection.mutableIntSetOf
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation

class SearchAdapter(private val context  : Context,private val musicList: List<Track>, private val onItemClick: (Track) -> Unit) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        private val download = DownloadManger(context)
        private val downingItem = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.song_name)
        val image: ImageView = view.findViewById(R.id.image)
        val download : ImageButton = view.findViewById(R.id.download)
        val progress : ProgressBar = view.findViewById(R.id.progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = musicList[position]
        holder.title.text = track.name
        holder.image.load(track.image) {
            crossfade(true)
            transformations(RoundedCornersTransformation(16f))
        }

        holder.itemView.setOnClickListener {
            onItemClick(track)
        }

        val isDownloaded = download.isTrackDownloaded(track.name,track.audio)
        val isDownloading = downingItem.contains(track.audio)

        when{
            isDownloaded -> {
                holder.download.setImageResource(R.drawable.baseline_file_download_done_24)
                holder.progress.visibility = View.GONE
            }
            isDownloading -> {
                holder.download.visibility = View.GONE
                holder.progress.visibility = View.VISIBLE
            }
            else -> {
                holder.download.setImageResource(R.drawable.baseline_file_download_24)
                holder.download.visibility = View.VISIBLE
                holder.progress.visibility = View.GONE
            }
        }

        holder.download.setOnClickListener {
            downloadTrack(track,holder)
        }
    }

    private fun downloadTrack(track : Track, holder : ViewHolder) {
        if (downingItem.contains(track.audio)){
            return
        }

        holder.download.visibility = View.GONE
        holder.progress.visibility = View.VISIBLE
        holder.progress.progress = 0

        downingItem.add(track.audio)

        download.downloadTrack(
            track = track,
            onProgress = { progress ->
                holder.itemView.post {
                    holder.progress.progress = progress
                }
            },
            onComplete = { success,path ->
                holder.itemView.post {
                    downingItem.remove(track.audio)

                    if (success){
                        holder.download.setImageResource(R.drawable.baseline_file_download_done_24)
                        holder.download.visibility = View.VISIBLE
                        holder.progress.visibility = View.GONE
                        Toast.makeText(context,"${track.name} 下載完成",Toast.LENGTH_SHORT).show()
                    }else{
                        holder.download.setImageResource(R.drawable.baseline_file_download_24)
                        holder.download.visibility = View.VISIBLE
                        holder.progress.visibility = View.GONE
                        Toast.makeText(context,"${track.name} 下載失敗",Toast.LENGTH_SHORT).show()
                    }
                    notifyItemChanged(musicList.indexOf(track))
                }
            }
        )
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}