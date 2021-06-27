package me.vanpetegem.accentor.ui.albums

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlin.math.max
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.main.MainActivity

class AlbumsFragment : Fragment() {

    private lateinit var viewModel: AlbumsViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(AlbumsViewModel::class.java)
        mediaSessionConnection = ViewModelProvider(requireActivity()).get(MediaSessionConnection::class.java)

        val cardView: FastScrollRecyclerView = view.findViewById(R.id.album_card_recycler_view)
        val viewAdapter = AlbumCardAdapter(
            this,
            object : AlbumActionListener {
                override fun play(album: Album) {
                    viewLifecycleOwner.lifecycleScope.launch(IO) { mediaSessionConnection.play(album) }
                }

                override fun playNext(album: Album) {
                    viewLifecycleOwner.lifecycleScope.launch(IO) {
                        mediaSessionConnection.addTracksToQueue(
                            album,
                            max(0, mediaSessionConnection.queuePosition.value ?: 0)
                        )
                    }
                }

                override fun playLast(album: Album) {
                    viewLifecycleOwner.lifecycleScope.launch(IO) {
                        mediaSessionConnection.addTracksToQueue(album)
                    }
                }
            }
        )
        val lm = GridLayoutManager(
            context,
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        )
        (activity as MainActivity).setCanChildScrollUpCallback(
            SwipeRefreshLayout.OnChildScrollUpCallback { _, _ ->
                lm.findFirstCompletelyVisibleItemPosition() > 0
            }
        )
        cardView.apply {
            setHasFixedSize(true)
            layoutManager = lm
            adapter = viewAdapter
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        cardView.layoutManager?.onSaveInstanceState()?.let { viewModel.saveScrollState(it) }
                    }
                }
            )
            setOnFastScrollStateChangeListener(
                object : OnFastScrollStateChangeListener {
                    override fun onFastScrollStop() {
                        cardView.layoutManager?.onSaveInstanceState()?.let { viewModel.saveScrollState(it) }
                    }

                    override fun onFastScrollStart() {}
                }
            )
        }
        viewModel.allAlbums.observe(
            viewLifecycleOwner,
            Observer {
                viewAdapter.items = it
            }
        )
        viewModel.scrollState.observe(
            viewLifecycleOwner,
            Observer {
                it?.let { cardView.layoutManager?.onRestoreInstanceState(it) }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).setCanChildScrollUpCallback(null)
    }
}
