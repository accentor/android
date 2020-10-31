package me.vanpetegem.accentor.ui.artists

import android.content.res.Configuration
import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_artists.view.*
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.main.MainActivity

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
        viewModel = ViewModelProvider(activity!!).get(ArtistsViewModel::class.java)

        val viewAdapter = ArtistCardAdapter(this)
        val lm = GridLayoutManager(
            context,
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        )
        (activity as MainActivity).setCanChildScrollUpCallback(SwipeRefreshLayout.OnChildScrollUpCallback { _, _ -> lm.findFirstCompletelyVisibleItemPosition() > 0 })
        view?.artistCardRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = lm
            adapter = viewAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    view?.artistCardRecyclerView?.layoutManager?.onSaveInstanceState()
                        ?.let { viewModel.saveScrollState(it) }
                }
            })
            setOnFastScrollStateChangeListener(object : OnFastScrollStateChangeListener {
                override fun onFastScrollStop() {
                    view?.artistCardRecyclerView?.layoutManager?.onSaveInstanceState()
                        ?.let { viewModel.saveScrollState(it) }
                }

                override fun onFastScrollStart() {}
            })
        }

        viewModel.allArtists.observe(viewLifecycleOwner, Observer {
            view?.artistCardRecyclerView?.apply {
                viewAdapter.items = it
            }
        })
        viewModel.scrollState.observe(viewLifecycleOwner, Observer {
            it?.let { view?.artistCardRecyclerView?.layoutManager?.onRestoreInstanceState(it) }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).setCanChildScrollUpCallback(null)
    }
}
