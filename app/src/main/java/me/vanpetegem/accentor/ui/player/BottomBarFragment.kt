package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.components.SquaredImageView
import me.vanpetegem.accentor.media.MediaSessionConnection

class BottomBarFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_bar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection = ViewModelProviders.of(this).get(MediaSessionConnection::class.java)

        val imageView: SquaredImageView = view!!.findViewById(R.id.album_cover_image_view)
        val trackTitle: TextView = view!!.findViewById(R.id.track_title)
        val albumTitle: TextView = view!!.findViewById(R.id.album_title)
        val pause = view!!.findViewById<SquaredImageView>(R.id.bottom_bar_pause)
            .apply { setOnClickListener { mediaSessionConnection.pause() } }
        val play = view!!.findViewById<SquaredImageView>(R.id.bottom_bar_play)
            .apply { setOnClickListener { mediaSessionConnection.play() } }
        view!!.findViewById<SquaredImageView>(R.id.bottom_bar_previous)
            .apply { setOnClickListener { mediaSessionConnection.previous() } }
        view!!.findViewById<SquaredImageView>(R.id.bottom_bar_next)
            .apply { setOnClickListener { mediaSessionConnection.next() } }


        mediaSessionConnection.currentAlbum.observe(this, Observer {
            albumTitle.text = it?.title ?: ""
            Glide.with(this)
                .load(it?.image)
                .placeholder(R.drawable.ic_menu_albums)
                .into(imageView)
        })

        mediaSessionConnection.currentTrack.observe(this, Observer {
            trackTitle.text = it?.title ?: ""
        })

        mediaSessionConnection.playing.observe(this, Observer {
            if (it != null && it) {
                play.visibility = View.GONE
                pause.visibility = View.VISIBLE
            } else {
                play.visibility = View.VISIBLE
                pause.visibility = View.GONE
            }
        })
    }
}

