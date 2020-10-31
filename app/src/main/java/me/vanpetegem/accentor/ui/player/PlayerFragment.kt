package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_player_view.view.*
import kotlinx.android.synthetic.main.player_views_holder.*
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.main.MainActivity
import me.vanpetegem.accentor.util.formatTrackLength

class PlayerFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            mediaSessionConnection.updateCurrentPosition()
            timerHandler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection =
            ViewModelProvider(activity!!).get(MediaSessionConnection::class.java)

        val adapter = PlayQueueAdapter {
            it?.let {
                mediaSessionConnection.skipTo(it)
                mediaSessionConnection.play()
            }
        }

        view?.queueRecyclerView?.adapter = adapter
        view?.queueRecyclerView?.layoutManager = LinearLayoutManager(context)
        val dragTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            var item: Triple<Boolean, Track?, Album?>? = null
            var newPosition = 0

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int =
                makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP
                )

            override fun onMove(
                _r: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ): Boolean {
                item = adapter.items[vh.adapterPosition]
                newPosition = t.adapterPosition
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                item?.second?.let { mediaSessionConnection.move(it, newPosition) }
            }
        })
        dragTouchHelper.attachToRecyclerView(view?.queueRecyclerView)
        val swipeTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int =
                makeFlag(
                    ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                )

            override fun onMove(
                _r: RecyclerView,
                _vh: RecyclerView.ViewHolder,
                _t: RecyclerView.ViewHolder
            ): Boolean =
                false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.items[viewHolder.adapterPosition].second?.let {
                    mediaSessionConnection.removeFromQueue(
                        it
                    )
                }
            }
        })
        swipeTouchHelper.attachToRecyclerView(view?.queueRecyclerView)

        (activity as MainActivity).playerToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.view_flipper_toggler -> {
                    view?.playerViewFlipper?.showNext()
                    true
                }
                R.id.clear_queue -> {
                    mediaSessionConnection.stop()
                    mediaSessionConnection.clearQueue()
                    true
                }
                else -> false
            }
        }

        view?.playControlsPause?.apply {
            setOnClickListener { mediaSessionConnection.pause() }
        }
        view?.playControlsPlay?.apply {
            setOnClickListener { mediaSessionConnection.play() }
        }
        view?.playControlsPrevious?.apply {
            setOnClickListener { mediaSessionConnection.previous() }
        }
        view?.playControlsNext?.apply {
            setOnClickListener { mediaSessionConnection.next() }
        }

        view?.playerSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                mediaSessionConnection.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        mediaSessionConnection.queue.observe(viewLifecycleOwner, {
            adapter.items = it ?: ArrayList()
        })

        mediaSessionConnection.playing.observe(viewLifecycleOwner, {
            if (it != null && it) {
                view?.playControlsPlay?.visibility = View.GONE
                view?.playControlsPause?.visibility = View.VISIBLE
            } else {
                view?.playControlsPlay?.visibility = View.VISIBLE
                view?.playControlsPause?.visibility = View.GONE
            }
        })

        mediaSessionConnection.buffering.observe(viewLifecycleOwner, {
            view?.playerSeekBar?.isEnabled = !(it ?: true)
        })

        mediaSessionConnection.currentAlbum.observe(viewLifecycleOwner, {
            view?.albumCoverImageView?.let { it1 ->
                Glide.with(this@PlayerFragment)
                    .load(it?.image500)
                    .placeholder(R.drawable.ic_menu_albums)
                    .into(it1)
            }

            view?.albumTitleView?.text = it?.title ?: ""
        })

        mediaSessionConnection.currentTrack.observe(viewLifecycleOwner, {
            view?.trackArtistsView?.text = it?.stringifyTrackArtists() ?: ""
            view?.trackTitleView?.text = it?.title ?: ""
            view?.playerTotalTime?.text = it?.length.formatTrackLength()
            view?.playerSeekBar?.max = it?.length ?: 0
        })

        mediaSessionConnection.currentPosition.observe(viewLifecycleOwner, {
            view?.playerCurrentTime?.text = it.formatTrackLength()
            view?.playerSeekBar?.setProgress(it ?: 0, false)
        })

        mediaSessionConnection.repeatMode.observe(viewLifecycleOwner, {
            when (it) {
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    view?.playControlsRepeat?.setImageResource(R.drawable.ic_repeat_all)
                    view?.playControlsRepeat?.setOnClickListener {
                        mediaSessionConnection.setRepeatMode(
                            PlaybackStateCompat.REPEAT_MODE_ONE
                        )
                    }
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    view?.playControlsRepeat?.setImageResource(R.drawable.ic_repeat_one)
                    view?.playControlsRepeat?.setOnClickListener {
                        mediaSessionConnection.setRepeatMode(
                            PlaybackStateCompat.REPEAT_MODE_NONE
                        )
                    }
                }
                else -> {
                    view?.playControlsRepeat?.setImageResource(R.drawable.ic_repeat_off)
                    view?.playControlsRepeat?.setOnClickListener {
                        mediaSessionConnection.setRepeatMode(
                            PlaybackStateCompat.REPEAT_MODE_ALL
                        )
                    }
                }
            }
        })

        mediaSessionConnection.shuffleMode.observe(viewLifecycleOwner, {
            when (it) {
                PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
                    view?.playControlsShuffle?.setImageResource(R.drawable.ic_shuffle_all)
                    view?.playControlsShuffle?.setOnClickListener {
                        mediaSessionConnection.setShuffleMode(
                            PlaybackStateCompat.SHUFFLE_MODE_NONE
                        )
                    }
                }
                else -> {
                    view?.playControlsShuffle?.setImageResource(R.drawable.ic_shuffle_none)
                    view?.playControlsShuffle?.setOnClickListener {
                        mediaSessionConnection.setShuffleMode(
                            PlaybackStateCompat.SHUFFLE_MODE_ALL
                        )
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        timerHandler.postDelayed(timerRunnable, 0)
    }

    override fun onPause() {
        super.onPause()
        timerHandler.removeCallbacks(timerRunnable)
    }
}
