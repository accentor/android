package me.vanpetegem.accentor.ui.albums

import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection

class AlbumsFragment : Fragment() {

    private lateinit var viewModel: AlbumsViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection
    private var allTracksByAlbumId = SparseArray<MutableList<Track>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.albums_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AlbumsViewModel::class.java)
        mediaSessionConnection = ViewModelProviders.of(this).get(MediaSessionConnection::class.java)

        val cardView: RecyclerView = view!!.findViewById(R.id.album_card_recycler_view)
        val viewAdapter = AlbumCardAdapter(this) { clickedItem ->
            mediaSessionConnection.play(
                allTracksByAlbumId.get(
                    clickedItem.id,
                    ArrayList()
                )
            )
        }
        cardView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            adapter = viewAdapter
        }
        viewModel.allAlbums.observe(this@AlbumsFragment, Observer {
            cardView.apply {
                viewAdapter.items = it
            }
        })
        viewModel.tracksByAlbumId.observe(this@AlbumsFragment, Observer {
            allTracksByAlbumId = it
        })

    }

}
