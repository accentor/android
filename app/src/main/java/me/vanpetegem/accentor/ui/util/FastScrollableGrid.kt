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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
    state: LazyGridState,
    width: Dp = 8.dp,
    minimumHeight: Dp = 48.dp,
    getSectionName: ((Int) -> String)
) {
    var dragging by remember { mutableStateOf(false) }
    val targetAlpha = if (state.isScrollInProgress || dragging) 1f else 0f
    val duration = if (state.isScrollInProgress || dragging) 150 else 500
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(duration)
    )
    val color = MaterialTheme.colorScheme.secondary
    val coroutineScope = rememberCoroutineScope()
    var scrollbarOffset by remember { mutableStateOf(0.dp) }
    val firstVisibleElementIndex by remember(state) {
        derivedStateOf { state.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
    }

    if (alpha > 0.0f && firstVisibleElementIndex != null) {
        val sectionName = getSectionName(firstVisibleElementIndex!!)

        val totalItemsCount by remember(state) { derivedStateOf { state.layoutInfo.totalItemsCount } }
        val itemHeight by remember(state) { derivedStateOf { state.layoutInfo.visibleItemsInfo[0].size.height } }
        val totalHeight = itemHeight * totalItemsCount
        val boxHeight by remember(state) { derivedStateOf { state.layoutInfo.viewportEndOffset } }
        val currentPosition by remember(state) {
            derivedStateOf { firstVisibleElementIndex!! * itemHeight + state.firstVisibleItemScrollOffset }
        }
        val topDistance = maxOf(0.dp, scrollbarOffset - (minimumHeight / 2))

        if (dragging) {
            Surface(
                modifier = Modifier.height(minimumHeight).width(minimumHeight).offset(-width * 2, topDistance),
                shape = RoundedCornerShape(50, 50, 0, 50),
                color = color
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(sectionName, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Canvas(
            modifier = Modifier.fillMaxHeight().width(width * 2).draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    val percentage = delta / boxHeight
                    val newPosition = maxOf(0f, currentPosition + percentage * totalHeight)
                    val newIndex = (newPosition / itemHeight).toInt()
                    val newOffset = (newPosition - (newIndex * itemHeight)).toInt()
                    coroutineScope.launch { state.scrollToItem(newIndex, newOffset) }
                },
                onDragStarted = { _ -> dragging = true },
                onDragStopped = { _ -> dragging = false }
            )
        ) {
            val scrollbarHeight = maxOf(boxHeight * (boxHeight.toFloat() / totalHeight), minimumHeight.toPx())
            val scrollbarDiff = maxOf(0f, scrollbarHeight - boxHeight * (boxHeight.toFloat() / totalHeight))
            val scrollbarOffsetY = (currentPosition.toFloat() / totalHeight * boxHeight) - (scrollbarDiff * (currentPosition.toFloat() / totalHeight))
            scrollbarOffset = (scrollbarOffsetY / 1.dp.toPx()).dp

            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2),
                topLeft = Offset(if (layoutDirection == LayoutDirection.Ltr) width.toPx() else 0.0f, scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}

@Composable
fun <T> FastScrollableGrid(gridItems: List<T>, getSectionName: (T) -> String, itemView: @Composable (T) -> Unit) {
    val gridState = rememberLazyGridState()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val cardsPerRow: Int = with(LocalDensity.current) { boxSize.width / 192.dp.toPx().toInt() }
    Box(Modifier.fillMaxSize(), Alignment.TopEnd) {
        LazyVerticalGrid(
            columns = if (cardsPerRow >= 2) GridCells.Adaptive(minSize = 192.dp) else GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.onGloballyPositioned { boxSize = it.size }
        ) {
            items(gridItems.size) { i -> itemView(gridItems[i]) }
        }
        if (gridItems.size / maxOf(cardsPerRow, 2) > 8) {
            ScrollBar(gridState, getSectionName = { getSectionName(gridItems[it]) })
        }
    }
}
