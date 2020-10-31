package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_bottom_bar.view.*
import me.vanpetegem.accentor.R
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection =
            ViewModelProvider(activity!!).get(MediaSessionConnection::class.java)

        view?.bottomBarPause?.apply {
            setOnClickListener { mediaSessionConnection.pause() }
        }
        view?.bottomBarPlay?.apply {
            setOnClickListener { mediaSessionConnection.play() }
        }
        view?.bottomBarPrevious?.apply {
            setOnClickListener { mediaSessionConnection.previous() }
        }
        view?.bottomBarNext?.apply {
            setOnClickListener { mediaSessionConnection.next() }
        }

        mediaSessionConnection.currentAlbum.observe(viewLifecycleOwner, {
            view?.albumCoverImageView?.let { it1 ->
                Glide.with(this)
                    .load(it?.image500)
                    .placeholder(R.drawable.ic_menu_albums)
                    .into(it1)
            }
        })

        mediaSessionConnection.currentTrack.observe(viewLifecycleOwner, {
            view?.trackTitle?.text = it?.title ?: ""
            view?.trackArtists?.text =
                it?.trackArtists?.sortedBy { ta -> ta.order }?.joinToString(" / ") { ta -> ta.name }
                    ?: ""
        })

        mediaSessionConnection.playing.observe(viewLifecycleOwner, {
            if (it != null && it) {
                view?.bottomBarPlay?.visibility = View.GONE
                view?.bottomBarPause?.visibility = View.VISIBLE
            } else {
                view?.bottomBarPlay?.visibility = View.VISIBLE
                view?.bottomBarPause?.visibility = View.GONE
            }
        })
    }
}
