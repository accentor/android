package me.vanpetegem.accentor.ui.albums

import android.content.res.Configuration
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.BaseMainFragment

class AlbumsFragment(callback: (SwipeRefreshLayout.OnChildScrollUpCallback?) -> Unit) : BaseMainFragment(callback) {

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
        viewModel = ViewModelProviders.of(activity!!).get(AlbumsViewModel::class.java)
        mediaSessionConnection = ViewModelProviders.of(activity!!).get(MediaSessionConnection::class.java)

        val cardView: FastScrollRecyclerView = view!!.findViewById(R.id.album_card_recycler_view)
        val viewAdapter = AlbumCardAdapter(this) { clickedItem ->
            mediaSessionConnection.play(
                allTracksByAlbumId.get(
                    clickedItem.id,
                    ArrayList()
                )
            )
        }
        val lm = GridLayoutManager(
            context,
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        )
        scrollCallback(SwipeRefreshLayout.OnChildScrollUpCallback { _, _ -> lm.findFirstCompletelyVisibleItemPosition() > 0 })
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
        scrollCallback(null)
    }
}
