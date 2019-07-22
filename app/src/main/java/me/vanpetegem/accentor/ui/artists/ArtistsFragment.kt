package me.vanpetegem.accentor.ui.artists

import android.content.res.Configuration
import android.os.Bundle
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
import me.vanpetegem.accentor.ui.BaseMainFragment

class ArtistsFragment(callback: (SwipeRefreshLayout.OnChildScrollUpCallback?) -> Unit) : BaseMainFragment(callback) {

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

        viewModel.allArtists.observe(viewLifecycleOwner, Observer {
            cardView.apply {
                viewAdapter.items = it
            }
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
