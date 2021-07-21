package me.vanpetegem.accentor.ui.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FixedThreshold
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.vanpetegem.accentor.media.MediaSessionConnection

@Composable
fun PlayerOverlay(
    navController: NavController,
    playerViewModel: PlayerViewModel = viewModel(),
    mediaSessionConnection: MediaSessionConnection = viewModel(),
    content: @Composable (() -> Unit)
) {
    val scope = rememberCoroutineScope()
    var totalHeight by remember { mutableStateOf<Int?>(null) }
    var toolbarHeight by remember { mutableStateOf(0) }
    val height = ((totalHeight ?: 0) - toolbarHeight).toFloat()
    val swipeableState = rememberSwipeableState(false) {
        playerViewModel.setOpen(it)
        true
    }
    val anchors = mapOf(0f to true, height to false)
    val showQueue by playerViewModel.showQueue.observeAsState()
    val queueLength by mediaSessionConnection.queueLength.observeAsState()
    val showPlayer = (queueLength ?: 0) > 0
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(queueLength) {
        if (queueLength == 0) {
            scope.launch { swipeableState.snapTo(false) }
        }
    }

    Box(modifier = Modifier.onSizeChanged { size -> totalHeight = size.height }) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = if (showPlayer) 56.dp else 0.dp)) {
            content()
        }
        if (totalHeight != null && showPlayer) {
            BackHandler(swipeableState.currentValue) {
                scope.launch {
                    swipeableState.animateTo(false, SwipeableDefaults.AnimationSpec)
                }
            }
            Column(
                modifier = Modifier
                    .offset { IntOffset(0, swipeableState.offset.value.toInt()) }
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .swipeable(
                            state = swipeableState,
                            anchors = anchors,
                            orientation = Orientation.Vertical,
                            thresholds = { _, _ -> FixedThreshold(224.dp) }
                        )
                        .onSizeChanged { size -> toolbarHeight = size.height }
                        .clickable {
                            scope.launch {
                                swipeableState.animateTo(!swipeableState.currentValue, SwipeableDefaults.AnimationSpec)
                            }
                        }
                ) {
                    if (swipeableState.currentValue) {
                        ToolBar(!isLandscape) {
                            scope.launch { swipeableState.animateTo(false, SwipeableDefaults.AnimationSpec) }
                        }
                    } else {
                        ControlBar()
                    }
                }
                Surface(Modifier.fillMaxSize()) {
                    if (isLandscape) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxHeight().weight(0.4f)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CurrentTrackInfo()
                                }
                                Controls()
                            }
                            Box(modifier = Modifier.fillMaxHeight().weight(0.6f)) {
                                Queue(navController)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (showQueue ?: false) {
                                    Queue(navController)
                                } else {
                                    CurrentTrackInfo()
                                }
                            }
                            Controls()
                        }
                    }
                }
            }
        }
    }
}
