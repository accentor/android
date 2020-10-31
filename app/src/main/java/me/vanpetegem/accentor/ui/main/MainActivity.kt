package me.vanpetegem.accentor.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.player_views_holder.*
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.albums.AlbumsFragment
import me.vanpetegem.accentor.ui.artists.ArtistsFragment
import me.vanpetegem.accentor.ui.home.HomeFragment
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.BottomBarFragment
import me.vanpetegem.accentor.ui.player.PlayerFragment
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    SwipeRefreshLayout.OnRefreshListener, SlidingUpPanelLayout.PanelSlideListener {

    companion object {
        const val INTENT_EXTRA_OPEN_PLAYER = "INTENT_EXTRA_OPEN_PLAYER"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        playerToolbar.apply {
            setNavigationIcon(R.drawable.ic_menu_back)
            setNavigationOnClickListener {
                slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            menuInflater.inflate(R.menu.player_toolbar_menu, menu)
            setTitle(R.string.now_playing)
            subtitle = "0/0"
        }

        val headerView: View = navView.getHeaderView(0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomBarFragmentHolder, BottomBarFragment()).commit()
        supportFragmentManager.beginTransaction().replace(R.id.player_view, PlayerFragment())
            .commit()

        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_back)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent500)

        slidingUpPanelLayout.addPanelSlideListener(this)
        navView.setNavigationItemSelectedListener(this)
        swipeRefreshLayout.setOnRefreshListener(this)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.loginState.observe(this, Observer {
            val loggedIn = it ?: return@Observer
            if (!loggedIn) {
                startActivity<LoginActivity>()
                finish()
            } else {
                mainViewModel.refresh()
            }
        })

        mainViewModel.currentUser.observe(this, {
            if (it == null) {
                headerView.navUsernameText.text = ""
            } else {
                headerView.navUsernameText.text = it.name
            }
        })

        mainViewModel.serverURL.observe(this, {
            if (it == null) {
                headerView.navServerURLText.text = ""
            } else {
                headerView.navServerURLText.text = it
            }
        })

        mainViewModel.navState.observe(this, Observer {
            val navState = it ?: return@Observer

            val transaction = supportFragmentManager.beginTransaction()
            val fragment = when (navState.fragmentId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_albums -> AlbumsFragment()
                R.id.nav_artists -> ArtistsFragment()
                else -> HomeFragment()
            }

            transaction.replace(R.id.main_fragment_container, fragment)
            if (navState.showBack) {
                transaction.addToBackStack(null)
            }
            transaction.commit()

            toggle.isDrawerIndicatorEnabled = !navState.showBack
            toggle.syncState()
        })

        mainViewModel.isRefreshing.observe(this, Observer {
            val refreshState = it ?: return@Observer

            swipeRefreshLayout.isRefreshing = refreshState
        })

        mainViewModel.isPlayerOpen.observe(this, Observer {
            val open = it ?: return@Observer
            if (open) {
                slidingUpPanelLayout.setDragView(playerToolbar)
                slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                bottomBarFragmentHolder.visibility = View.GONE
                playerToolbar.visibility = View.VISIBLE

                toggle.syncState()
            } else {
                slidingUpPanelLayout.setDragView(bottomBarFragmentHolder)
                slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                bottomBarFragmentHolder.visibility = View.VISIBLE
                playerToolbar.visibility = View.GONE
                supportActionBar?.apply { setDisplayShowTitleEnabled(true) }
                toggle.syncState()
            }
        })

        mediaSessionConnection = ViewModelProvider(this).get(MediaSessionConnection::class.java)
        mediaSessionConnection.queuePosStr.observe(this, {
            playerToolbar.subtitle = it ?: "0/0"
        })
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { checkIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent) {
        if (intent.hasExtra(INTENT_EXTRA_OPEN_PLAYER)) {
            mainViewModel.setPlayerOpen(intent.getBooleanExtra(INTENT_EXTRA_OPEN_PLAYER, false))
            intent.removeExtra(INTENT_EXTRA_OPEN_PLAYER)
        }
    }

    override fun onBackPressed() {
        when {
            slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED -> slidingUpPanelLayout.panelState =
                SlidingUpPanelLayout.PanelState.COLLAPSED
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                mainViewModel.logout()
                true
            }
            R.id.action_refresh -> {
                if (mainViewModel.isRefreshing.value == false) {
                    mainViewModel.refresh()
                    true
                } else {
                    false
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        mainViewModel.navigate(item.itemId)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRefresh() {
        mainViewModel.refresh()
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
    }

    override fun onPanelStateChanged(
        panel: View,
        previousState: SlidingUpPanelLayout.PanelState,
        newState: SlidingUpPanelLayout.PanelState
    ) {
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            mainViewModel.setPlayerOpen(false)
        } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mainViewModel.setPlayerOpen(true)
        }
    }

    fun setCanChildScrollUpCallback(callback: SwipeRefreshLayout.OnChildScrollUpCallback?) =
        swipeRefreshLayout.setOnChildScrollUpCallback(callback)
}
