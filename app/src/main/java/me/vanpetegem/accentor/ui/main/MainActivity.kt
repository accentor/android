package me.vanpetegem.accentor.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.albums.AlbumGrid
import me.vanpetegem.accentor.ui.artists.ArtistGrid
import me.vanpetegem.accentor.ui.home.Home
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.PlayerOverlay
import me.vanpetegem.accentor.ui.player.PlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccentorTheme() {
                Content()
            }
        }
    }
}

@Composable
fun Content(mainViewModel: MainViewModel = viewModel(), playerViewModel: PlayerViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentNavigation by navController.currentBackStackEntryAsState()
    val isPlayerOpen by playerViewModel.isOpen.observeAsState()

    val loginState by mainViewModel.loginState.observeAsState()
    val context = LocalContext.current
    LaunchedEffect(loginState) {
        val loggedIn = loginState
        if (loggedIn != null) {
            if (!loggedIn) {
                context.startActivity(Intent(context, LoginActivity::class.java))
                (context as Activity).finish()
            } else {
                mainViewModel.refresh()
            }
        }
    }

    PlayerOverlay() {
        Scaffold(
            scaffoldState = scaffoldState,
            drawerContent = {
                DrawerRow(stringResource(R.string.home), currentNavigation?.destination?.route == "home", R.drawable.ic_menu_home) {
                    navController.navigate("home")
                    scope.launch { scaffoldState.drawerState.close() }
                }
                DrawerRow(stringResource(R.string.menu_artists), currentNavigation?.destination?.route == "artists", R.drawable.ic_menu_artists) {
                    navController.navigate("artists")
                    scope.launch { scaffoldState.drawerState.close() }
                }
                DrawerRow(stringResource(R.string.menu_albums), currentNavigation?.destination?.route == "albums", R.drawable.ic_menu_albums) {
                    navController.navigate("albums")
                    scope.launch { scaffoldState.drawerState.close() }
                }
            },
            drawerGesturesEnabled = !(isPlayerOpen ?: false),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.open_drawer))
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.height(40.dp).aspectRatio(1f).wrapContentSize(Alignment.TopStart)) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.open_menu))
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    onClick = {
                                        mainViewModel.refresh()
                                        expanded = false
                                    }
                                ) {
                                    Text(stringResource(R.string.action_refresh))
                                }
                                DropdownMenuItem(
                                    onClick = {
                                        mainViewModel.logout()
                                        expanded = false
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
            val isRefreshing by mainViewModel.isRefreshing.observeAsState()
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing ?: false),
                onRefresh = { mainViewModel.refresh() },
                indicator = { state, trigger -> SwipeRefreshIndicator(state, trigger, contentColor = MaterialTheme.colors.secondary) }
            ) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { Home() }
                    composable("artists") { ArtistGrid() }
                    composable("albums") { AlbumGrid() }
                }
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
            Icon(painterResource(icon), contentDescription = stringResource(R.string.navigation_icon), tint = textColor)
            Text(title, modifier = Modifier.padding(16.dp, 8.dp), color = textColor)
        }
    }
}
