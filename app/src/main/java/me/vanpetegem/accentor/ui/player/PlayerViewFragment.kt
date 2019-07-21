package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection

class PlayerViewFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.player_view_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection = ViewModelProviders.of(this).get(MediaSessionConnection::class.java)
    }
}
