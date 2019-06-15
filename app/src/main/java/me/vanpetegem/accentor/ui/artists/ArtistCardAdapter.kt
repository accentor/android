package me.vanpetegem.accentor.ui.artists

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.artists.Artist

class ArtistCardAdapter : RecyclerView.Adapter<ArtistCardAdapter.ViewHolder>() {
    var items: List<Artist> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(
        gridView: CardView,
        val artistNameView: TextView
    ) : RecyclerView.ViewHolder(gridView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val gridView = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_card_view, parent, false) as CardView

        val artistNameView: TextView = gridView.findViewById(R.id.artist_card_name_view)

        return ViewHolder(gridView, artistNameView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.artistNameView.text = items[position].name
    }
}