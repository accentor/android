package me.vanpetegem.accentor.ui.player

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun PlayerOverlay(
    navController: NavController,
    playerViewModel: PlayerViewModel = viewModel(),
    content: @Composable (() -> Unit),
) {
    val scope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp
    var totalHeight by remember { mutableIntStateOf(screenHeight) }
    var toolbarHeight by remember { mutableIntStateOf(0) }
    val height = (totalHeight - toolbarHeight).toFloat()
    val showQueue by playerViewModel.showQueue.observeAsState()
    val queueLength by playerViewModel.queueLength.observeAsState()
    val showPlayer = (queueLength ?: 0) > 0
    val isLandscape = (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
    val isMultiWindow = (LocalContext.current as Activity).isInMultiWindowMode()
    val anchors =
        DraggableAnchors {
            true at 0f
            false at height
        }
    val velThreshold = with(LocalDensity.current) { 125.dp.toPx() }
    val posThreshold = with(LocalDensity.current) { 224.dp.toPx() }
    val anchoredDraggableState =
        remember {
            AnchoredDraggableState(
                initialValue = false,
                anchors = anchors,
                positionalThreshold = { posThreshold },
                velocityThreshold = { velThreshold },
                animationSpec = SpringSpec<Float>(),
            ) {
                playerViewModel.setOpen(it)
                true
            }
        }
    val closePlayer: () -> Unit = { scope.launch { anchoredDraggableState.animateTo(false) } }

    Box(
        modifier =
            Modifier.onSizeChanged { size ->
                totalHeight = size.height
                anchoredDraggableState.updateAnchors(
                    DraggableAnchors {
                        true at 0f
                        false at (size.height - toolbarHeight).toFloat()
                    },
                )
            },
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = if (showPlayer) 56.dp else 0.dp)) {
            content()
        }
        if (showPlayer) {
            BackHandler(anchoredDraggableState.currentValue, closePlayer)
            Column(
                modifier =
                    Modifier
                        .offset { IntOffset(0, anchoredDraggableState.requireOffset().toInt()) }
                        .fillMaxSize(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .anchoredDraggable(state = anchoredDraggableState, orientation = Orientation.Vertical)
                            .onSizeChanged { size ->
                                toolbarHeight = size.height
                                anchoredDraggableState.updateAnchors(
                                    DraggableAnchors {
                                        true at 0f
                                        false at (totalHeight - size.height).toFloat()
                                    },
                                )
                            }
                            .clickable {
                                scope.launch {
                                    anchoredDraggableState.animateTo(!anchoredDraggableState.currentValue)
                                }
                            },
                ) {
                    if (anchoredDraggableState.currentValue) {
                        ToolBar(!(isLandscape && !isMultiWindow), closePlayer = closePlayer)
                    } else {
                        ControlBar()
                    }
                }
                Surface(Modifier.fillMaxSize()) {
                    if (isLandscape && !isMultiWindow) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.fillMaxHeight().weight(0.4f)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CurrentTrackInfo()
                                }
                                Controls()
                            }
                            Box(modifier = Modifier.fillMaxHeight().weight(0.6f)) {
                                Queue(navController, closePlayer = closePlayer)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (showQueue ?: false) {
                                    Queue(navController, closePlayer = closePlayer)
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
