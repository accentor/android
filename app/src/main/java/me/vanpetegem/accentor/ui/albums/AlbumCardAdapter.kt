package me.vanpetegem.accentor.ui.albums

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album

class AlbumCardAdapter : RecyclerView.Adapter<AlbumCardAdapter.ViewHolder>() {
    var items: List<Album> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(
        gridView: CardView,
        val albumTitleView: TextView,
        val albumImageView: ImageView
    ) : RecyclerView.ViewHolder(gridView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val gridView = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_card_view, parent, false) as CardView

        val albumTitleView: TextView = gridView.findViewById(R.id.album_card_title_view)
        val imageView: ImageView = gridView.findViewById(R.id.album_card_image_view)

        return ViewHolder(gridView, albumTitleView, imageView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.albumTitleView.text = items[position].title
        items[position].image ?: holder.albumImageView.setImageResource(R.drawable.ic_menu_albums)
        items[position].image?.let {
            Picasso.get().load(it).placeholder(R.drawable.ic_menu_albums).into(holder.albumImageView)
        }
    }
}