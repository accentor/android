package me.vanpetegem.accentor.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Home() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Home")
        }
    }
}
