package me.vanpetegem.accentor.ui.player

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.util.formatTrackLength
import org.jetbrains.anko.sdk27.coroutines.onClick

class PlayQueueAdapter(val clickHandler: (Track) -> Unit) : RecyclerView.Adapter<PlayQueueAdapter.ViewHolder>() {

    class ViewHolder(
        val root: View,
        val trackTitleView: TextView,
        val trackArtistsView: TextView,
        val trackLengthView: TextView,
        val playingIndicator: ImageView
    ) : RecyclerView.ViewHolder(root)

    var items: List<Triple<Boolean, Track, Album>> = ArrayList()
        set(value) {
            field = value
            Log.d("PLAYQUEUE", "Queue length: ${value.size}")
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.queue_list_item, parent, false)

        val trackTitleView = root.findViewById<TextView>(R.id.track_title)
        val trackArtistsView = root.findViewById<TextView>(R.id.track_artists)
        val trackLengthView = root.findViewById<TextView>(R.id.track_length)
        val playingIndicator = root.findViewById<ImageView>(R.id.playing_indicator)

        return ViewHolder(root, trackTitleView, trackArtistsView, trackLengthView, playingIndicator)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.root.onClick { clickHandler(items[position].second) }
        holder.trackTitleView.text = items[position].second.title
        val trackArtists = items[position].second.stringifyTrackArtists()
        holder.trackArtistsView.text = if (trackArtists.isEmpty()) items[position].second.title else trackArtists
        holder.trackLengthView.text = items[position].second.length.formatTrackLength()
        holder.playingIndicator.visibility = if (items[position].first) View.VISIBLE else View.GONE
    }

}
