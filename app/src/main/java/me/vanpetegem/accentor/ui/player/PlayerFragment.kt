package me.vanpetegem.accentor.ui.player

import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.components.SquaredImageView
import me.vanpetegem.accentor.data.albums.Album
import me.vanpetegem.accentor.data.tracks.Track
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.main.MainActivity
import me.vanpetegem.accentor.util.formatTrackLength

class PlayerFragment : Fragment() {
    private lateinit var mediaSessionConnection: MediaSessionConnection
    private val timerHandler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            mediaSessionConnection.updateCurrentPosition()
            timerHandler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaSessionConnection = ViewModelProviders.of(activity!!).get(MediaSessionConnection::class.java)

        val playQueueView = view!!.findViewById<RecyclerView>(R.id.queue_recycler_view)
        val adapter = PlayQueueAdapter { mediaSessionConnection.skipTo(it) }
        playQueueView.adapter = adapter
        playQueueView.layoutManager = LinearLayoutManager(context)
        val dragTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            var item: Triple<Boolean, Track, Album>? = null
            var newPosition = 0

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
                makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP
                )

            override fun onMove(_r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean {
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

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                item?.let { mediaSessionConnection.move(it.second, newPosition) }

            }
        })
        dragTouchHelper.attachToRecyclerView(playQueueView)
        val swipeTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)

            override fun onMove(_r: RecyclerView, _vh: RecyclerView.ViewHolder, _t: RecyclerView.ViewHolder): Boolean =
                false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
                mediaSessionConnection.removeFromQueue(adapter.items[viewHolder.adapterPosition].second)

        })
        swipeTouchHelper.attachToRecyclerView(playQueueView)

        val viewFlipper: ViewFlipper = view!!.findViewById(R.id.player_view_flipper)
        (activity as MainActivity).playerToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.view_flipper_toggler -> {
                    viewFlipper.showNext()
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

        val albumCoverImageView: ImageView = view!!.findViewById(R.id.album_cover_image_view)
        val albumTitleView: TextView = view!!.findViewById(R.id.album_title_view)
        val trackTitleView: TextView = view!!.findViewById(R.id.track_title_view)
        val trackArtistsView: TextView = view!!.findViewById(R.id.track_artists_view)
        val currentTimeView: TextView = view!!.findViewById(R.id.player_current_time)
        val seekBar: SeekBar = view!!.findViewById(R.id.player_seek_bar)
        val fullLengthView: TextView = view!!.findViewById(R.id.player_total_time)
        val pause = view!!.findViewById<SquaredImageView>(R.id.play_controls_pause)
            .apply { setOnClickListener { mediaSessionConnection.pause() } }
        val play = view!!.findViewById<SquaredImageView>(R.id.play_controls_play)
            .apply { setOnClickListener { mediaSessionConnection.play() } }
        view!!.findViewById<SquaredImageView>(R.id.play_controls_previous)
            .apply { setOnClickListener { mediaSessionConnection.previous() } }
        view!!.findViewById<SquaredImageView>(R.id.play_controls_next)
            .apply { setOnClickListener { mediaSessionConnection.next() } }
        val repeatModeIndicator = view!!.findViewById<SquaredImageView>(R.id.play_controls_repeat)
        val shuffleModeIndicator = view!!.findViewById<SquaredImageView>(R.id.play_controls_shuffle)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                mediaSessionConnection.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        mediaSessionConnection.queue.observe(viewLifecycleOwner, Observer {
            adapter.items = it ?: ArrayList()
        })

        mediaSessionConnection.playing.observe(viewLifecycleOwner, Observer {
            if (it != null && it) {
                play.visibility = View.GONE
                pause.visibility = View.VISIBLE
            } else {
                play.visibility = View.VISIBLE
                pause.visibility = View.GONE
            }
        })

        mediaSessionConnection.buffering.observe(viewLifecycleOwner, Observer {
            seekBar.isEnabled = !(it ?: true)
        })

        mediaSessionConnection.currentAlbum.observe(viewLifecycleOwner, Observer {
            Glide.with(this@PlayerFragment)
                .load(it?.image500)
                .placeholder(R.drawable.ic_menu_albums)
                .into(albumCoverImageView)

            albumTitleView.text = it?.title ?: ""
        })

        mediaSessionConnection.currentTrack.observe(viewLifecycleOwner, Observer {
            trackArtistsView.text = it?.stringifyTrackArtists() ?: ""
            trackTitleView.text = it?.title ?: ""
            fullLengthView.text = it?.length.formatTrackLength()
            seekBar.max = it?.length ?: 0
        })

        mediaSessionConnection.currentPosition.observe(viewLifecycleOwner, Observer {
            currentTimeView.text = it.formatTrackLength()
            seekBar.setProgress(it ?: 0, false)
        })

        mediaSessionConnection.repeatMode.observe(viewLifecycleOwner, Observer {
            when (it) {
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    repeatModeIndicator.setImageResource(R.drawable.ic_repeat_all)
                    repeatModeIndicator.setOnClickListener { mediaSessionConnection.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE) }
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    repeatModeIndicator.setImageResource(R.drawable.ic_repeat_one)
                    repeatModeIndicator.setOnClickListener { mediaSessionConnection.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE) }
                }
                else -> {
                    repeatModeIndicator.setImageResource(R.drawable.ic_repeat_off)
                    repeatModeIndicator.setOnClickListener { mediaSessionConnection.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL) }
                }
            }
        })

        mediaSessionConnection.shuffleMode.observe(viewLifecycleOwner, Observer {
            when (it) {
                PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
                    shuffleModeIndicator.setImageResource(R.drawable.ic_shuffle_all)
                    shuffleModeIndicator.setOnClickListener { mediaSessionConnection.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE) }
                }
                else -> {
                    shuffleModeIndicator.setImageResource(R.drawable.ic_shuffle_none)
                    shuffleModeIndicator.setOnClickListener { mediaSessionConnection.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL) }
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
