package me.vanpetegem.accentor.ui.artists

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.vanpetegem.accentor.R

class ArtistsFragment : Fragment() {

    private lateinit var viewModel: ArtistsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ArtistsViewModel::class.java)

        val cardView: FastScrollRecyclerView = view!!.findViewById(R.id.artist_card_recycler_view)
        val viewAdapter = ArtistCardAdapter(this)
        cardView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                context,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
            )
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

        viewModel.allArtists.observe(this@ArtistsFragment, Observer {
            cardView.apply {
                viewAdapter.items = it
            }
        })
        viewModel.scrollState.observe(viewLifecycleOwner, Observer {
            it?.let { cardView.layoutManager?.onRestoreInstanceState(it) }
        })
    }

}
