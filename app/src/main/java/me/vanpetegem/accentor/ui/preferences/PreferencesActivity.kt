package me.vanpetegem.accentor.ui.preferences

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import me.vanpetegem.accentor.ui.AccentorTheme

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
fun Content() {
    Text("Preferences")
}
