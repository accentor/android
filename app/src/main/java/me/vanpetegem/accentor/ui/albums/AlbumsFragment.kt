package me.vanpetegem.accentor.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.vanpetegem.accentor.R

class AlbumsFragment : Fragment() {

    private lateinit var viewModel: AlbumsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.albums_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AlbumsViewModel::class.java)

        val cardView: RecyclerView = view!!.findViewById(R.id.album_card_recycler_view)
        val viewAdapter = AlbumCardAdapter(this)
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

    }

}
