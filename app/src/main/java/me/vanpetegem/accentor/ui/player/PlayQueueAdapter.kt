package me.vanpetegem.accentor.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track

class PlayQueueAdapter : BaseAdapter() {

    var items: List<Triple<Boolean, Track, Album>> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): Triple<Boolean, Track, Album> = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount(): Int = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.queue_list_item, parent, false)

        val trackTitleView = view.findViewById<TextView>(R.id.track_title)
        trackTitleView.text = items[position].second.title

        val trackArtistsView = view.findViewById<TextView>(R.id.track_artists)
        val trackArtists = items[position].second.stringifyTrackArtists()
        trackArtistsView.text = if (trackArtists.isEmpty()) items[position].second.title else trackArtists

        val trackLengthView = view.findViewById<TextView>(R.id.track_length)
        trackLengthView.text = items[position].second.length.formatTrackLength()

        val playingIndicator = view.findViewById<ImageView>(R.id.playing_indicator)
        playingIndicator.visibility = if (items[position].first) View.VISIBLE else View.GONE

        return view
    }

}

private fun Int?.formatTrackLength(): String? =
    if (this == null)
        "0:00"
    else
        "${this / 60}:${(this % 60).toString().padStart(2, '0')}"
