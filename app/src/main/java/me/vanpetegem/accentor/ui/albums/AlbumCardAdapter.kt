package me.vanpetegem.accentor.ui.albums

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import org.jetbrains.anko.sdk27.coroutines.onClick

interface AlbumActionListener {
    fun play(album: Album)
    fun playNext(album: Album)
    fun playLast(album: Album)
}

class AlbumCardAdapter(private val fragment: Fragment, private val actionListener: AlbumActionListener) :
    RecyclerView.Adapter<AlbumCardAdapter.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    var items: List<Album> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(
        cardView: CardView,
        val albumTitleView: TextView,
        val albumSubtitleView: TextView,
        val albumImageView: ImageView,
        val menu: PopupMenu
    ) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val gridView = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_card_view, parent, false) as CardView

        val albumTitleView: TextView = gridView.findViewById(R.id.album_card_title_view)
        val albumSubtitleView: TextView = gridView.findViewById(R.id.album_card_subtitle_view)
        val imageView: ImageView = gridView.findViewById(R.id.album_card_image_view)
        val menuButton: ImageButton = gridView.findViewById(R.id.album_card_menu_button)
        val menu = PopupMenu(fragment.context, menuButton, Gravity.END).apply { inflate(R.menu.album_card_menu) }
        menuButton.onClick { menu.show() }

        return ViewHolder(gridView, albumTitleView, albumSubtitleView, imageView, menu)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.albumTitleView.text = items[position].title
        holder.albumSubtitleView.text =
            items[position].stringifyAlbumArtists()
                .let { if (it.isEmpty()) fragment.getString(R.string.various_artists) else it }
        Glide.with(fragment)
            .load(items[position].image500)
            .placeholder(R.drawable.ic_menu_albums)
            .into(holder.albumImageView)
        holder.menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.album_play_now -> {
                    actionListener.play(items[position])
                    true
                }
                R.id.album_play_next -> {
                    actionListener.playNext(items[position])
                    true
                }
                R.id.album_play_last -> {
                    actionListener.playLast(items[position])
                    true
                }
                else -> false
            }
        }
    }

    override fun getSectionName(position: Int): String = "${items[position].title[0].toUpperCase()}"
}