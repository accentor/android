package me.vanpetegem.accentor.ui.artists

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.artists.Artist

class ArtistCardAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<ArtistCardAdapter.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var items: List<Artist> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(
        gridView: CardView,
        val artistNameView: TextView,
        val artistImageView: ImageView
    ) : RecyclerView.ViewHolder(gridView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val gridView = LayoutInflater.from(parent.context)
            .inflate(R.layout.artist_card_view, parent, false) as CardView

        val artistNameView: TextView = gridView.findViewById(R.id.artist_card_name_view)
        val imageView: ImageView = gridView.findViewById(R.id.artist_card_image_view)

        return ViewHolder(gridView, artistNameView, imageView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.artistNameView.text = items[position].name
        Glide.with(fragment)
            .load(items[position].image500)
            .placeholder(R.drawable.ic_artist)
            .into(holder.artistImageView)
    }

    override fun getSectionName(position: Int): String = "${String(intArrayOf(items[position].name.codePointAt(0)), 0, 1).uppercase()}"
}
