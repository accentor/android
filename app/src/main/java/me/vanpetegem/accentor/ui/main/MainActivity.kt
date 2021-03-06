package me.vanpetegem.accentor.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.albums.AlbumsFragment
import me.vanpetegem.accentor.ui.artists.ArtistsFragment
import me.vanpetegem.accentor.ui.home.HomeFragment
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.BottomBarFragment
import me.vanpetegem.accentor.ui.player.PlayerFragment

class MainActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    SwipeRefreshLayout.OnRefreshListener,
    SlidingUpPanelLayout.PanelSlideListener {

    companion object {
        const val INTENT_EXTRA_OPEN_PLAYER = "INTENT_EXTRA_OPEN_PLAYER"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    lateinit var playerToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val mainToolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mainToolbar)

        slidingUpPanelLayout = findViewById(R.id.sliding_layout)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            mainToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        playerToolbar = findViewById<Toolbar>(R.id.player_toolbar).apply {
            setNavigationIcon(R.drawable.ic_menu_back)
            setNavigationOnClickListener { slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED }
            menuInflater.inflate(R.menu.player_toolbar_menu, menu)
            setTitle(R.string.now_playing)
            subtitle = "0/0"
        }

        val headerView: View = navView.getHeaderView(0)
        val usernameText: TextView = headerView.findViewById(R.id.nav_header_username)
        val serverURLText: TextView = headerView.findViewById(R.id.nav_header_server_url)
        val bottomBarFragmentHolder: FrameLayout = findViewById(R.id.bottom_bar)
        supportFragmentManager.beginTransaction().replace(R.id.bottom_bar, BottomBarFragment()).commit()
        supportFragmentManager.beginTransaction().replace(R.id.player_view, PlayerFragment()).commit()

        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_back)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent500)

        slidingUpPanelLayout.addPanelSlideListener(this)
        navView.setNavigationItemSelectedListener(this)
        swipeRefreshLayout.setOnRefreshListener(this)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.loginState.observe(
            this,
            Observer {
                val loggedIn = it ?: return@Observer
                if (!loggedIn) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    mainViewModel.refresh()
                }
            }
        )

        mainViewModel.currentUser.observe(
            this,
            Observer {
                if (it == null) {
                    usernameText.text = ""
                } else {
                    usernameText.text = it.name
                }
            }
        )

        mainViewModel.serverURL.observe(
            this,
            Observer {
                if (it == null) {
                    serverURLText.text = ""
                } else {
                    serverURLText.text = it
                }
            }
        )

        mainViewModel.navState.observe(
            this,
            Observer {
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
            }
        )

        mainViewModel.isRefreshing.observe(
            this,
            Observer {
                val refreshState = it ?: return@Observer

                swipeRefreshLayout.isRefreshing = refreshState
            }
        )

        mainViewModel.isPlayerOpen.observe(
            this,
            Observer {
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
            }
        )

        mediaSessionConnection = ViewModelProvider(this).get(MediaSessionConnection::class.java)
        mediaSessionConnection.queuePosStr.observe(
            this,
            Observer {
                playerToolbar.subtitle = it ?: "0/0"
            }
        )
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
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val slidingUpPanelLayout: SlidingUpPanelLayout = findViewById(R.id.sliding_layout)
        when {
            slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ->
                slidingUpPanelLayout.panelState =
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
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
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
