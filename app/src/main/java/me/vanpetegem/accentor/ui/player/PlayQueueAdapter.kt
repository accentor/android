package me.vanpetegem.accentor.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track

class PlayQueueAdapter : BaseAdapter() {

    var items: List<Pair<Track, Album>> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(position: Int): Pair<Track, Album> = items[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount(): Int = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.queue_list_item, parent, false)

        val trackTitleView = view.findViewById<TextView>(R.id.track_title)
        trackTitleView.text = items[position].first.title

        return view
    }

}
