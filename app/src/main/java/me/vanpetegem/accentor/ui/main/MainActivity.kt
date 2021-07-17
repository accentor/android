package me.vanpetegem.accentor.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.media.MediaSessionConnection
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.albums.AlbumGrid
import me.vanpetegem.accentor.ui.artists.ArtistGrid
import me.vanpetegem.accentor.ui.home.Home
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.BottomBarFragment
import me.vanpetegem.accentor.ui.player.PlayerFragment

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    companion object {
        const val INTENT_EXTRA_OPEN_PLAYER = "INTENT_EXTRA_OPEN_PLAYER"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mediaSessionConnection: MediaSessionConnection
    lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    lateinit var playerToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        slidingUpPanelLayout = findViewById(R.id.sliding_layout)
        playerToolbar = findViewById<Toolbar>(R.id.player_toolbar).apply {
            setNavigationIcon(R.drawable.ic_menu_back)
            setNavigationOnClickListener { slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED }
            menuInflater.inflate(R.menu.player_toolbar_menu, menu)
            setTitle(R.string.now_playing)
            subtitle = "0/0"
        }

        val bottomBarFragmentHolder: FrameLayout = findViewById(R.id.bottom_bar)
        supportFragmentManager.beginTransaction().replace(R.id.bottom_bar, BottomBarFragment()).commit()
        supportFragmentManager.beginTransaction().replace(R.id.player_view, PlayerFragment()).commit()

        slidingUpPanelLayout.addPanelSlideListener(this)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.loginState.observe(
            this,
            Observer {
                val loggedIn = it ?: return@Observer
                if (!loggedIn) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
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
                } else {
                    slidingUpPanelLayout.setDragView(bottomBarFragmentHolder)
                    slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    bottomBarFragmentHolder.visibility = View.VISIBLE
                    playerToolbar.visibility = View.GONE
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

        findViewById<ComposeView>(R.id.compose_view).setContent {
            AccentorTheme {
                Content()
            }
        }
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
            slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ->
                slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            else -> super.onBackPressed()
        }
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
}

@Composable
fun Content(mainViewModel: MainViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentNavigation = navController.currentBackStackEntryAsState()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            DrawerRow(stringResource(R.string.home), currentNavigation.value?.destination?.route == "home", R.drawable.ic_menu_home) {
                navController.navigate("home")
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow(stringResource(R.string.menu_artists), currentNavigation.value?.destination?.route == "artists", R.drawable.ic_menu_artists) {
                navController.navigate("artists")
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow(stringResource(R.string.menu_albums), currentNavigation.value?.destination?.route == "albums", R.drawable.ic_menu_albums) {
                navController.navigate("albums")
                scope.launch { scaffoldState.drawerState.close() }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                actions = {
                    val expanded = remember { mutableStateOf(false) }
                    Box(modifier = Modifier.height(40.dp).aspectRatio(1f).wrapContentSize(Alignment.TopStart)) {
                        IconButton(onClick = { expanded.value = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                            DropdownMenuItem(
                                onClick = {
                                    mainViewModel.refresh()
                                    expanded.value = false
                                }
                            ) {
                                Text(stringResource(R.string.action_refresh))
                            }
                            DropdownMenuItem(
                                onClick = {
                                    mainViewModel.logout()
                                    expanded.value = false
                                }
                            ) {
                                Text(stringResource(R.string.action_sign_out))
                            }
                        }
                    }
                }
            )
        },
    ) { _ ->
        val isRefreshing = mainViewModel.isRefreshing.observeAsState()
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing.value ?: false),
            onRefresh = { mainViewModel.refresh() },
        ) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { Home() }
                composable("artists") { ArtistGrid() }
                composable("albums") { AlbumGrid() }
            }
        }
    }
}

@Composable
fun DrawerRow(title: String, selected: Boolean, @DrawableRes icon: Int, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent
    val textColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    ListItem(modifier = Modifier.clickable(onClick = onClick).background(background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = null, tint = textColor)
            Text(title, modifier = Modifier.padding(16.dp, 8.dp), color = textColor)
        }
    }
}
