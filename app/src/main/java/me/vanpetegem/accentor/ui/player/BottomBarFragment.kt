package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.components.SquaredImageView
import me.vanpetegem.accentor.media.MediaSessionConnection

class BottomBarFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaSessionConnection = ViewModelProvider(requireActivity()).get(MediaSessionConnection::class.java)

        val imageView: SquaredImageView = view.findViewById(R.id.album_cover_image_view)
        val trackTitle: TextView = view.findViewById(R.id.track_title)
        val trackArtists: TextView = view.findViewById(R.id.track_artists)
        val pause = view.findViewById<SquaredImageView>(R.id.bottom_bar_pause).apply {
            setOnClickListener { doDelayed { mediaSessionConnection.pause() } }
        }
        val play = view.findViewById<SquaredImageView>(R.id.bottom_bar_play).apply {
            setOnClickListener { doDelayed { mediaSessionConnection.play() } }
        }
        view.findViewById<SquaredImageView>(R.id.bottom_bar_previous).apply {
            setOnClickListener { doDelayed { mediaSessionConnection.previous() } }
        }
        view.findViewById<SquaredImageView>(R.id.bottom_bar_next).apply {
            setOnClickListener { doDelayed { mediaSessionConnection.next() } }
        }

        mediaSessionConnection.currentAlbum.observe(
            viewLifecycleOwner,
            Observer {
                Glide.with(this)
                    .load(it?.image500)
                    .placeholder(R.drawable.ic_menu_albums)
                    .into(imageView)
            }
        )

        mediaSessionConnection.currentTrack.observe(
            viewLifecycleOwner,
            Observer {
                trackTitle.text = it?.title ?: ""
                trackArtists.text =
                    it?.trackArtists?.sortedBy { ta -> ta.order }?.joinToString(" / ") { ta -> ta.name } ?: ""
            }
        )

        mediaSessionConnection.playing.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null && it) {
                    play.visibility = View.GONE
                    pause.visibility = View.VISIBLE
                } else {
                    play.visibility = View.VISIBLE
                    pause.visibility = View.GONE
                }
            }
        )
    }

    private fun doDelayed(block: suspend CoroutineScope.() -> Unit) = viewLifecycleOwner.lifecycleScope.launch(IO, block = block)
}
