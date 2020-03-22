package me.vanpetegem.accentor.ui.albums

import android.content.res.Configuration
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.main.MainActivity
import kotlin.math.max

class AlbumsFragment : Fragment() {

    private lateinit var viewModel: AlbumsViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection
    private var allTracksByAlbumId = SparseArray<MutableList<Track>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(AlbumsViewModel::class.java)
        mediaSessionConnection = ViewModelProvider(activity!!).get(MediaSessionConnection::class.java)

        val cardView: FastScrollRecyclerView = view!!.findViewById(R.id.album_card_recycler_view)
        val viewAdapter = AlbumCardAdapter(this, object : AlbumActionListener {
            override fun play(album: Album) {
                mediaSessionConnection.play(
                    allTracksByAlbumId.get(
                        album.id,
                        ArrayList()
                    ).map { Pair(it, album) }
                )
            }

            override fun playNext(album: Album) {
                mediaSessionConnection.addTracksToQueue(
                    allTracksByAlbumId.get(
                        album.id,
                        ArrayList()
                    ).map { Pair(it, album) },
                    max(0, mediaSessionConnection.queuePosition.value ?: 0)
                )
            }

            override fun playLast(album: Album) {
                mediaSessionConnection.addTracksToQueue(
                    allTracksByAlbumId.get(
                        album.id,
                        ArrayList()
                    ).map { Pair(it, album) }
                )
            }

        })
        val lm = GridLayoutManager(
            context,
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        )
        (activity as MainActivity).setCanChildScrollUpCallback(SwipeRefreshLayout.OnChildScrollUpCallback { _, _ -> lm.findFirstCompletelyVisibleItemPosition() > 0 })
        cardView.apply {
            setHasFixedSize(true)
            layoutManager = lm
            adapter = viewAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    cardView.layoutManager?.onSaveInstanceState()?.let { viewModel.saveScrollState(it) }
                }
            })
            setOnFastScrollStateChangeListener(object : OnFastScrollStateChangeListener {
                override fun onFastScrollStop() {
                    cardView.layoutManager?.onSaveInstanceState()?.let { viewModel.saveScrollState(it) }
                }

                override fun onFastScrollStart() {}
            })
        }
        viewModel.allAlbums.observe(viewLifecycleOwner, Observer {
            viewAdapter.items = it
        })
        viewModel.tracksByAlbumId.observe(viewLifecycleOwner, Observer {
            allTracksByAlbumId = it
        })
        viewModel.scrollState.observe(viewLifecycleOwner, Observer {
            it?.let { cardView.layoutManager?.onRestoreInstanceState(it) }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).setCanChildScrollUpCallback(null)
    }
}
