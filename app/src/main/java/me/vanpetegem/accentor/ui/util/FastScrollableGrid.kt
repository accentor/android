package me.vanpetegem.accentor.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ScrollBar(
    state: LazyListState,
    scrollableSize: IntSize,
    width: Dp = 8.dp,
    minimumHeight: Dp = 48.dp,
    getSectionName: ((Int) -> String)
) {
    var dragging by remember { mutableStateOf(false) }
    val targetAlpha = if (state.isScrollInProgress || dragging) 1f else 0f
    val duration = if (state.isScrollInProgress || dragging) 150 else 500
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(duration),
    )
    val color = MaterialTheme.colors.secondary
    val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
    val totalItemsCount = state.layoutInfo.totalItemsCount
    val coroutineScope = rememberCoroutineScope()
    var scrollbarOffset by remember { mutableStateOf(0.dp) }

    if (alpha > 0.0f && firstVisibleElementIndex != null) {
        val sectionName = getSectionName(firstVisibleElementIndex)
        val topDistance = maxOf(0.dp, scrollbarOffset - (minimumHeight / 2))
        if (dragging) {
            Surface(
                modifier = Modifier.height(minimumHeight).width(minimumHeight).offset(-width * 2, topDistance),
                shape = RoundedCornerShape(50, 50, 0, 50),
                color = color,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(sectionName, style = MaterialTheme.typography.h5)
                }
            }
        }

        Canvas(
            modifier = Modifier.fillMaxHeight().width(width * 2).draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    val percentage = delta / scrollableSize.height
                    coroutineScope.launch {
                        state.scrollToItem(maxOf(0, firstVisibleElementIndex + (percentage * totalItemsCount).toInt()), 0)
                    }
                },
                onDragStarted = { _ -> dragging = true },
                onDragStopped = { _ -> dragging = false },
            )
        ) {
            val baseElementHeight = scrollableSize.height.toFloat() / state.layoutInfo.totalItemsCount
            val scrollbarHeight = maxOf(state.layoutInfo.visibleItemsInfo.size * baseElementHeight, minimumHeight.toPx())
            val elementHeight = (scrollableSize.height.toFloat() - scrollbarHeight) / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            scrollbarOffset = (scrollbarOffsetY / 1.dp.toPx()).dp

            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2),
                topLeft = Offset(if (layoutDirection == LayoutDirection.Ltr) width.toPx() else 0.0f, scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha,
            )
        }
    }
}

@Composable
fun <T> FastScrollableGrid(gridItems: List<T>, getSectionName: (T) -> String, itemView: @Composable (T) -> Unit) {
    val listState = rememberLazyListState()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val cardsPerRow: Int = with(LocalDensity.current) { boxSize.width / 192.dp.toPx().toInt() }
    Box(Modifier.fillMaxSize(), Alignment.TopEnd) {
        LazyVerticalGrid(
            cells = if (cardsPerRow >= 2) GridCells.Adaptive(minSize = 192.dp) else GridCells.Fixed(2),
            state = listState,
            modifier = Modifier.onGloballyPositioned { boxSize = it.size },
        ) {
            items(gridItems.size) { i -> itemView(gridItems[i]) }
        }
        ScrollBar(listState, boxSize, getSectionName = { getSectionName(gridItems[it * cardsPerRow]) })
    }
}
