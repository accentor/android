package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection

class PlayerViewFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection = ViewModelProviders.of(activity!!).get(MediaSessionConnection::class.java)

        val listView = view!!.findViewById<ListView>(R.id.queue_list_view)
        val adapter = PlayQueueAdapter()
        listView.adapter = adapter

        mediaSessionConnection.queue.observe(viewLifecycleOwner, Observer {
            adapter.items = it ?: ArrayList()
        })
    }
}
