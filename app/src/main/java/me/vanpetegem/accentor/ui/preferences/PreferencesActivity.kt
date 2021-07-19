package me.vanpetegem.accentor.ui.preferences

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.version

class PreferencesActivity : ComponentActivity() {
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
fun Content(preferencesViewModel: PreferencesViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preferences)) },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.close_preferences))
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentUser by preferencesViewModel.currentUser.observeAsState()
        val server by preferencesViewModel.server.observeAsState()
        val imageCacheSize by preferencesViewModel.imageCacheSize.observeAsState()
        val musicCacheSize by preferencesViewModel.musicCacheSize.observeAsState()
        val conversionId by preferencesViewModel.conversionId.observeAsState()
        Column(modifier = Modifier.padding(innerPadding)) {
            Setting(stringResource(R.string.logged_in_as, "${currentUser?.name}"), server!!)
            Header(stringResource(R.string.playback_settings))
            var musicCacheOpen by remember { mutableStateOf(false) }
            var newMusicCacheValue by remember { mutableStateOf("${musicCacheSize!! / 1024L / 1024L}") }
            val musicCacheSizeValid = (newMusicCacheValue.toLongOrNull() ?: 0) > 1024L
            Setting(stringResource(R.string.music_cache_size), "${musicCacheSize!! / 1024L / 1024L} MiB") {
                musicCacheOpen = true
            }
            SettingDialog(
                musicCacheOpen,
                stringResource(R.string.change_music_cache_size),
                musicCacheSizeValid,
                { preferencesViewModel.setMusicCacheSize(newMusicCacheValue.toLong() * 1024L * 1024L) },
                { musicCacheOpen = false },
            ) {
                Column {
                    Text(stringResource(R.string.music_cache_explanation), modifier = Modifier.padding(bottom = 16.dp))
                    OutlinedTextField(newMusicCacheValue, { newMusicCacheValue = it }, isError = !musicCacheSizeValid)
                }
            }
            Divider()
            var imageCacheOpen by remember { mutableStateOf(false) }
            var newImageCacheValue by remember { mutableStateOf("${imageCacheSize!! / 1024L / 1024L}") }
            val imageCacheSizeValid = (newImageCacheValue.toLongOrNull() ?: 0) > 100L
            Setting(stringResource(R.string.image_cache_size), "${imageCacheSize!! / 1024L / 1024L} MiB") {
                imageCacheOpen = true
            }
            SettingDialog(
                imageCacheOpen,
                stringResource(R.string.change_image_cache_size),
                imageCacheSizeValid,
                { preferencesViewModel.setImageCacheSize(newImageCacheValue.toLong() * 1024L * 1024L) },
                { imageCacheOpen = false },
            ) {
                Column {
                    Text(stringResource(R.string.image_cache_explanation), modifier = Modifier.padding(bottom = 16.dp))
                    OutlinedTextField(newImageCacheValue, { newImageCacheValue = it }, isError = !imageCacheSizeValid)
                }
            }
            Divider()
            Setting(stringResource(R.string.codec_conversion), conversionId ?: stringResource(R.string.not_set))
            Header(stringResource(R.string.about))
            Setting(stringResource(R.string.version_info, version))
        }
    }
}

@Composable
fun Header(text: String) {
    Text(
        text,
        color = MaterialTheme.colors.secondary,
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp)
    )
}

@Composable
fun Setting(text: String, subtext: String? = null, onClick: (() -> Unit) = {}) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Text(text, modifier = Modifier.padding(top = 8.dp, start = 8.dp, bottom = if (subtext != null) 0.dp else 8.dp))
        if (subtext != null) {
            Text(
                subtext,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                style = MaterialTheme.typography.body2,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
            )
        }
    }
}

@Composable
fun SettingDialog(
    opened: Boolean,
    title: String,
    canSave: Boolean,
    save: (() -> Unit),
    dismiss: (() -> Unit),
    content: @Composable () -> Unit
) {
    if (opened) {
        AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(title, style = MaterialTheme.typography.h6) },
            text = content,
            dismissButton = {
                TextButton(onClick = dismiss) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(onClick = { save(); dismiss() }, enabled = canSave) { Text(stringResource(R.string.save)) }
            },
        )
    }
}
