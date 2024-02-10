package me.vanpetegem.accentor.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.albums.AlbumGrid
import me.vanpetegem.accentor.ui.albums.AlbumToolbar
import me.vanpetegem.accentor.ui.albums.AlbumView
import me.vanpetegem.accentor.ui.albums.AlbumViewDropdown
import me.vanpetegem.accentor.ui.artists.ArtistGrid
import me.vanpetegem.accentor.ui.artists.ArtistToolbar
import me.vanpetegem.accentor.ui.artists.ArtistView
import me.vanpetegem.accentor.ui.home.Home
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.PlayerOverlay
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.playlists.PlaylistList
import me.vanpetegem.accentor.ui.playlists.PlaylistToolbar
import me.vanpetegem.accentor.ui.playlists.PlaylistView
import me.vanpetegem.accentor.ui.preferences.PreferencesActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccentorTheme {
                Content()
            }
        }
    }
}

@Composable
fun Content(
    mainViewModel: MainViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel(),
) {
    val navController = rememberNavController()

    val loginState by mainViewModel.loginState.observeAsState()
    val context = LocalContext.current

    var isFirstRender by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(loginState) {
        val loggedIn = loginState
        if (loggedIn != null) {
            if (!loggedIn) {
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as Activity).finish()
            } else if (isFirstRender) {
                isFirstRender = false
                mainViewModel.refresh()
            }
        }
    }

    val latestError by mainViewModel.latestError.observeAsState()
    LaunchedEffect(latestError) {
        val error = latestError?.get()
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    PlayerOverlay(navController) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { Base(navController, mainViewModel, playerViewModel) { Home(navController, playerViewModel) } }
            composable("artists") {
                Base(
                    navController,
                    mainViewModel,
                    playerViewModel,
                    toolbar = { ArtistToolbar(it, mainViewModel) },
                ) { ArtistGrid(navController) }
            }
            composable("artists/{artistId}", arguments = listOf(navArgument("artistId") { type = NavType.IntType })) { entry ->
                Base(navController, mainViewModel, playerViewModel) { ArtistView(entry.arguments!!.getInt("artistId"), navController, playerViewModel) }
            }
            composable("albums") {
                Base(
                    navController,
                    mainViewModel,
                    playerViewModel,
                    toolbar = { AlbumToolbar(it, mainViewModel) },
                ) { AlbumGrid(navController, playerViewModel) }
            }
            composable("albums/{albumId}", arguments = listOf(navArgument("albumId") { type = NavType.IntType })) { entry ->
                Base(
                    navController,
                    mainViewModel,
                    playerViewModel,
                    toolbar = { drawerState ->
                        BaseToolbar(
                            drawerState,
                            mainViewModel,
                            extraDropdownItems = {
                                AlbumViewDropdown(entry.arguments!!.getInt("albumId"), navController, it)
                            },
                        )
                    },
                ) {
                    AlbumView(entry.arguments!!.getInt("albumId"), navController, playerViewModel)
                }
            }
            composable("playlists") {
                Base(
                    navController,
                    mainViewModel,
                    playerViewModel,
                    toolbar = { PlaylistToolbar(it, mainViewModel) },
                ) { PlaylistList(navController, playerViewModel) }
            }
            composable("playlists/{playlistId}", arguments = listOf(navArgument("playlistId") { type = NavType.IntType })) { entry ->
                Base(navController, mainViewModel, playerViewModel) {
                    PlaylistView(entry.arguments!!.getInt("playlistId"), navController, playerViewModel)
                }
            }
        }
    }
}

@Composable
fun Base(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel(),
    toolbar: @Composable ((DrawerState) -> Unit) = { drawerState -> BaseToolbar(drawerState, mainViewModel) },
    mainContent: @Composable (() -> Unit),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentNavigation by navController.currentBackStackEntryAsState()
    val isPlayerOpen by playerViewModel.isOpen.observeAsState()
    val context = LocalContext.current
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }
    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                DrawerRow(stringResource(R.string.home), currentNavigation?.destination?.route == "home", R.drawable.ic_menu_home) {
                    navController.navigate("home")
                    scope.launch { drawerState.close() }
                }
                DrawerRow(stringResource(R.string.artists), currentNavigation?.destination?.route == "artists", R.drawable.ic_menu_artists) {
                    navController.navigate("artists")
                    scope.launch { drawerState.close() }
                }
                DrawerRow(stringResource(R.string.albums), currentNavigation?.destination?.route == "albums", R.drawable.ic_menu_albums) {
                    navController.navigate("albums")
                    scope.launch { drawerState.close() }
                }
                DrawerRow(stringResource(R.string.playlists), currentNavigation?.destination?.route == "playlists", R.drawable.ic_menu_playlists) {
                    navController.navigate("playlists")
                    scope.launch { drawerState.close() }
                }
                HorizontalDivider()
                DrawerRow(stringResource(R.string.preferences), false, R.drawable.ic_menu_preferences) {
                    context.startActivity(Intent(context, PreferencesActivity::class.java))
                    scope.launch { drawerState.close() }
                }
            }
        },
        gesturesEnabled = !(isPlayerOpen ?: false),
    ) {
        Scaffold(
            topBar = { toolbar(drawerState) },
        ) { contentPadding ->
            val isRefreshing by mainViewModel.isRefreshing.observeAsState()
            val state = rememberPullRefreshState(isRefreshing ?: false, { mainViewModel.refresh() })
            Box(modifier = Modifier.pullRefresh(state).padding(contentPadding)) {
                mainContent()
                PullRefreshIndicator(isRefreshing ?: false, state, Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun BaseToolbar(
    drawerState: DrawerState,
    mainViewModel: MainViewModel = viewModel(),
    extraActions: @Composable (() -> Unit)? = null,
    extraDropdownItems: @Composable ((() -> Unit) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            if (drawerState.isOpen) {
                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.close_app_drawer))
                }
            } else {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.open_drawer))
                }
            }
        },
        actions = {
            extraActions?.invoke()
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.height(40.dp).aspectRatio(1f).wrapContentSize(Alignment.TopStart)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.open_menu))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (extraDropdownItems != null) {
                        extraDropdownItems { expanded = false }
                    }
                    DropdownMenuItem(
                        onClick = {
                            mainViewModel.refresh()
                            expanded = false
                        },
                        text = { Text(stringResource(R.string.action_refresh)) },
                    )
                    DropdownMenuItem(
                        onClick = {
                            mainViewModel.logout()
                            expanded = false
                        },
                        text = { Text(stringResource(R.string.action_sign_out)) },
                    )
                }
            }
        },
    )
}

@Composable
fun SearchToolbar(
    value: String,
    update: (String) -> Unit,
    exit: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = { exit() },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.stop_searching))
            }
        },
        title = {
            TextField(
                value,
                update,
                singleLine = true,
                placeholder = {
                    Text(
                        stringResource(R.string.search),
                        color = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.primaryContainer),
                        fontWeight = FontWeight.Normal,
                    )
                },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = LocalContentColor.current,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusRequester.freeFocus()
                        },
                    ),
            )
        },
    )
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
    BackHandler { exit() }
}

@Composable
fun DrawerRow(
    title: String,
    selected: Boolean,
    icon: Int,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(title, modifier = Modifier.padding(16.dp, 8.dp)) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        icon = { Icon(painterResource(icon), contentDescription = stringResource(R.string.navigation_icon)) },
    )
}
