package me.vanpetegem.accentor.ui.albums

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
import me.vanpetegem.accentor.data.albums.Album
import org.jetbrains.anko.sdk27.coroutines.onClick

class AlbumCardAdapter(private val fragment: Fragment, private val clickHandler: (Album) -> Unit) :
    RecyclerView.Adapter<AlbumCardAdapter.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    var items: List<Album> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(
        val cardView: CardView,
        val albumTitleView: TextView,
        val albumSubtitleView: TextView,
        val albumImageView: ImageView
    ) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val gridView = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_card_view, parent, false) as CardView

        val albumTitleView: TextView = gridView.findViewById(R.id.album_card_title_view)
        val albumSubtitleView: TextView = gridView.findViewById(R.id.album_card_subtitle_view)
        val imageView: ImageView = gridView.findViewById(R.id.album_card_image_view)

        return ViewHolder(gridView, albumTitleView, albumSubtitleView, imageView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cardView.onClick { clickHandler(items[position]) }
        holder.albumTitleView.text = items[position].title
        holder.albumSubtitleView.text =
            items[position].stringifyAlbumArtists()
                .let { if (it.isEmpty()) fragment.getString(R.string.various_artists) else it }
        Glide.with(fragment)
            .load(items[position].image)
            .placeholder(R.drawable.ic_menu_albums)
            .into(holder.albumImageView)
    }

    override fun getSectionName(position: Int): String = "${items[position].title[0].toUpperCase()}"
}