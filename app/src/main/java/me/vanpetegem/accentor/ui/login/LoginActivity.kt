package me.vanpetegem.accentor.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.vanpetegem.accentor.R
import me.vanpetegem.accentor.ui.AccentorTheme
import me.vanpetegem.accentor.ui.main.MainActivity

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccentorTheme {
                Content()
            }
        }
    }
}

@Composable
fun Content(loginViewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()

    suspend fun tryLogin(
        server: String,
        username: String,
        password: String,
    ) {
        val result: LoginResult = loginViewModel.login(server, username, password)
        withContext(Main) {
            if (result.error != null) {
                Toast.makeText(context, resources.getString(result.error), Toast.LENGTH_LONG).show()
            } else {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as Activity).finish()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.sign_in)) })
        },
        content = { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val server = rememberTextFieldState("https://")
                val username = rememberTextFieldState()
                val password = rememberTextFieldState()
                LaunchedEffect(username, server, password) {
                    snapshotFlow {
                        listOf(
                            server.text.toString(),
                            username.text.toString(),
                            password.text.toString(),
                        )
                    }.collectLatest {
                        loginViewModel.loginDataChanged(it[0], it[1], it[2])
                    }
                }
                val formState by loginViewModel.loginFormState.observeAsState()
                val usernameFocusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    state = server,
                    modifier =
                        Modifier
                            .semantics {
                                if (formState?.serverError != null) {
                                    error(resources.getString(formState!!.serverError!!))
                                }
                            }.fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    label = { Text(stringResource(R.string.prompt_server)) },
                    isError = formState?.serverError != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions =
                        KeyboardOptions(
                            autoCorrectEnabled = false,
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Uri,
                        ),
                    onKeyboardAction = {
                        usernameFocusRequester.requestFocus()
                    },
                )
                val passwordFocusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    state = username,
                    label = { Text(stringResource(R.string.prompt_username)) },
                    modifier =
                        Modifier
                            .semantics { contentType = ContentType.Username }
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                            .focusRequester(usernameFocusRequester),
                    keyboardOptions =
                        KeyboardOptions(
                            autoCorrectEnabled = false,
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Next,
                        ),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    onKeyboardAction = {
                        passwordFocusRequester.requestFocus()
                    },
                )
                val keyboardController = LocalSoftwareKeyboardController.current
                val loading by loginViewModel.loading.observeAsState()
                OutlinedSecureTextField(
                    state = password,
                    label = { Text(stringResource(R.string.prompt_password)) },
                    modifier =
                        Modifier
                            .semantics { contentType = ContentType.Password }
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                            .focusRequester(passwordFocusRequester),
                    keyboardOptions =
                        KeyboardOptions(
                            autoCorrectEnabled = false,
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        scope.launch(IO) { tryLogin(server.text.toString(), username.text.toString(), password.text.toString()) }
                    },
                )
                Button(
                    onClick = {
                        keyboardController?.hide()
                        scope.launch(IO) { tryLogin(server.text.toString(), username.text.toString(), password.text.toString()) }
                    },
                    enabled = formState?.isDataValid ?: false,
                    modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                ) {
                    Text(stringResource(R.string.sign_in), style = MaterialTheme.typography.labelLarge)
                }
                if (loading ?: false) {
                    CircularProgressIndicator()
                }
            }
        },
    )
}
