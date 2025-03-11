package com.example.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation

class OfflineTrackAdapter(
    private var track: List<OfflineTrack>,
    private val onTrackClick: (OfflineTrack,Int) -> Unit,
    private val onTrackDelete: (OfflineTrack) -> Unit,
): RecyclerView.Adapter<OfflineTrackAdapter.ViewHolder>() {
    class ViewHolder(view : View): RecyclerView.ViewHolder(view) {
        val names = view.findViewById<TextView>(R.id.songs)
        val delete = view.findViewById<ImageButton>(R.id.delete)
        val images = view.findViewById<ImageView>(R.id.images)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.offline_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tracks = track[position]

        holder.names.text = tracks.name
        holder.images.load(tracks.image){
            crossfade(true)
            transformations(RoundedCornersTransformation(16f))
        }

        holder.itemView.setOnClickListener {
            onTrackClick(tracks,position)
        }

        holder.delete.setOnClickListener {
            onTrackDelete(tracks)
        }
    }

    override fun getItemCount(): Int {
        return track.size
    }

    fun updateTracks(newTracks : List<OfflineTrack>){
        track = newTracks
        notifyDataSetChanged()
    }
}