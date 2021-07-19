package me.vanpetegem.accentor.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Timer(delayTime: Long = 100L, onTick: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    DisposableEffect(onTick) {
        var running = true
        scope.launch(IO) {
            while (running) {
                delay(delayTime)
                onTick()
            }
        }
        onDispose { running = false }
    }
}
