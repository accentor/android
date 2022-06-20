package me.vanpetegem.accentor.ui.main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.devices.Device
import me.vanpetegem.accentor.devices.DeviceManager
import me.vanpetegem.accentor.devices.DeviceService
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.albums.AlbumGrid
import me.vanpetegem.accentor.ui.albums.AlbumToolbar
import me.vanpetegem.accentor.ui.albums.AlbumView
import me.vanpetegem.accentor.ui.albums.AlbumViewDropdown
import me.vanpetegem.accentor.ui.artists.ArtistGrid
import me.vanpetegem.accentor.ui.artists.ArtistToolbar
import me.vanpetegem.accentor.ui.artists.ArtistView
import me.vanpetegem.accentor.ui.devices.Devices
import me.vanpetegem.accentor.ui.home.Home
import me.vanpetegem.accentor.ui.login.LoginActivity
import me.vanpetegem.accentor.ui.player.PlayerOverlay
import me.vanpetegem.accentor.ui.player.PlayerViewModel
import me.vanpetegem.accentor.ui.preferences.PreferencesActivity
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.FixedAndroidLogHandler
import org.fourthline.cling.model.message.header.ServiceTypeHeader
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.model.types.UDN
import org.seamless.util.logging.LoggingUtil
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var deviceManager: DeviceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccentorTheme() {
                Content()
            }
        }

        // Fix the logging integration between java.util.logging and Android internal logging
        LoggingUtil.resetRootHandler(FixedAndroidLogHandler())
        //Logger.getLogger("org.fourthline.cling").level = Level.FINE

        applicationContext.bindService(Intent(this, DeviceService::class.java), deviceManager.connection, Context.BIND_AUTO_CREATE)
    }
}

@Composable
fun Content(mainViewModel: MainViewModel = viewModel(), playerViewModel: PlayerViewModel = viewModel()) {
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

    PlayerOverlay(navController) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { Base(navController, mainViewModel, playerViewModel) { Home(navController, playerViewModel) } }
            composable("artists") {
                Base(
                    navController, mainViewModel, playerViewModel, toolbar = { ArtistToolbar(it, mainViewModel) }
                ) { ArtistGrid(navController) }
            }
            composable("artists/{artistId}", arguments = listOf(navArgument("artistId") { type = NavType.IntType })) { entry ->
                Base(navController, mainViewModel, playerViewModel) { ArtistView(entry.arguments!!.getInt("artistId"), navController, playerViewModel) }
            }
            composable("albums") {
                Base(
                    navController, mainViewModel, playerViewModel, toolbar = { AlbumToolbar(it, mainViewModel) }
                ) { AlbumGrid(navController, playerViewModel) }
            }
            composable("albums/{albumId}", arguments = listOf(navArgument("albumId") { type = NavType.IntType })) { entry ->
                Base(
                    navController,
                    mainViewModel,
                    playerViewModel,
                    toolbar = { scaffoldState ->
                        BaseToolbar(
                            scaffoldState,
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
            composable("devices") { Base(navController, mainViewModel, playerViewModel) { Devices() } }
        }
    }
}

@Composable
fun Base(
    navController: NavController,
    mainViewModel: MainViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel(),
    toolbar: @Composable ((ScaffoldState) -> Unit) = { scaffoldState -> BaseToolbar(scaffoldState, mainViewModel) },
    mainContent: @Composable (() -> Unit),
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val currentNavigation by navController.currentBackStackEntryAsState()
    val isPlayerOpen by playerViewModel.isOpen.observeAsState()
    val context = LocalContext.current
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            DrawerRow(stringResource(R.string.home), currentNavigation?.destination?.route == "home", R.drawable.ic_menu_home) {
                navController.navigate("home")
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow(stringResource(R.string.artists), currentNavigation?.destination?.route == "artists", R.drawable.ic_menu_artists) {
                navController.navigate("artists")
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow(stringResource(R.string.albums), currentNavigation?.destination?.route == "albums", R.drawable.ic_menu_albums) {
                navController.navigate("albums")
                scope.launch { scaffoldState.drawerState.close() }
            }
            DrawerRow(stringResource(R.string.devices), currentNavigation?.destination?.route == "devices", R.drawable.ic_menu_devices) {
                navController.navigate("devices")
                scope.launch { scaffoldState.drawerState.close() }
            }
            Divider()
            DrawerRow(stringResource(R.string.preferences), false, R.drawable.ic_menu_preferences) {
                context.startActivity(Intent(context, PreferencesActivity::class.java))
                scope.launch { scaffoldState.drawerState.close() }
            }
        },
        drawerGesturesEnabled = !(isPlayerOpen ?: false),
        topBar = { toolbar(scaffoldState) },
    ) { _ ->
        val isRefreshing by mainViewModel.isRefreshing.observeAsState()
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing ?: false),
            onRefresh = { mainViewModel.refresh() },
            indicator = { state, trigger -> SwipeRefreshIndicator(state, trigger, contentColor = MaterialTheme.colors.secondary) },
            content = mainContent,
        )
    }
}

@Composable
fun BaseToolbar(
    scaffoldState: ScaffoldState,
    mainViewModel: MainViewModel = viewModel(),
    extraActions: @Composable (() -> Unit)? = null,
    extraDropdownItems: @Composable ((() -> Unit) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.open_drawer))
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
        },
    )
}

@Composable
fun SearchToolbar(value: String, update: (String) -> Unit, exit: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    TopAppBar(contentPadding = PaddingValues(0.dp)) {
        IconButton(
            onClick = { exit() },
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.stop_searching))
        }
        TextField(
            value,
            update,
            singleLine = true,
            placeholder = {
                Text(
                    stringResource(R.string.search),
                    color = MaterialTheme.colors.contentColorFor(MaterialTheme.colors.primarySurface).copy(ContentAlpha.medium)
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = LocalContentColor.current.copy(LocalContentAlpha.current),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.weight(1f).fillMaxHeight().focusRequester(focusRequester),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                },
            ),
        )
    }
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
    BackHandler { exit() }
}

@Composable
fun DrawerRow(title: String, selected: Boolean, icon: Int, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent
    val textColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    ListItem(modifier = Modifier.clickable(onClick = onClick).background(background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = stringResource(R.string.navigation_icon), tint = textColor)
            Text(title, modifier = Modifier.padding(16.dp, 8.dp), color = textColor)
        }
    }
}
