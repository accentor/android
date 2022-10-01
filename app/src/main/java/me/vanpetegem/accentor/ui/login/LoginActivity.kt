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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.Autofill
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillTree
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
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
            AccentorTheme() {
                Content()
            }
        }
    }
}

@Composable
fun Content(loginViewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun tryLogin(server: String, username: String, password: String) {
        val result: LoginResult = loginViewModel.login(server, username, password)
        withContext(Main) {
            if (result.error != null) {
                Toast.makeText(context, context.getString(result.error), Toast.LENGTH_LONG).show()
            } else {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as Activity).finish()
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text(stringResource(R.string.sign_in)) })
        },
        content = { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var server by rememberSaveable { mutableStateOf("https://") }
                var username by rememberSaveable { mutableStateOf("") }
                var password by rememberSaveable { mutableStateOf("") }
                val formState by loginViewModel.loginFormState.observeAsState()
                val usernameFocusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    value = server,
                    onValueChange = { value ->
                        server = value
                        loginViewModel.loginDataChanged(server, username, password)
                    },
                    modifier = Modifier.semantics {
                        if (formState?.serverError != null) {
                            error(context.getString(formState!!.serverError!!))
                        }
                    }.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    label = { Text(stringResource(R.string.prompt_server)) },
                    isError = !(formState?.serverError == null),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Uri,
                    ),
                    keyboardActions = KeyboardActions(onNext = { usernameFocusRequester.requestFocus() })
                )
                val passwordFocusRequester = remember { FocusRequester() }
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        loginViewModel.loginDataChanged(server, username, password)
                    },
                    label = { Text(stringResource(R.string.prompt_username)) },
                    modifier = Modifier.autofill(LocalAutofill.current, LocalAutofillTree.current, listOf(AutofillType.Username)) {
                        username = it
                        loginViewModel.loginDataChanged(server, username, password)
                    }.fillMaxWidth().padding(start = 16.dp, end = 16.dp).focusRequester(usernameFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                    singleLine = true,
                )
                val keyboardController = LocalSoftwareKeyboardController.current
                val loading by loginViewModel.loading.observeAsState()
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        loginViewModel.loginDataChanged(server, username, password)
                    },
                    label = { Text(stringResource(R.string.prompt_password)) },
                    singleLine = true,
                    modifier = Modifier.autofill(LocalAutofill.current, LocalAutofillTree.current, listOf(AutofillType.Password)) {
                        password = it
                        loginViewModel.loginDataChanged(server, username, password)
                    }.fillMaxWidth().padding(start = 16.dp, end = 16.dp).focusRequester(passwordFocusRequester),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                        scope.launch(IO) { tryLogin(server, username, password) }
                    },
                )
                Button(
                    onClick = {
                        keyboardController?.hide()
                        scope.launch(IO) { tryLogin(server, username, password) }
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
        }
    )
}

fun Modifier.autofill(
    autofill: Autofill?,
    autofillTree: AutofillTree,
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
): Modifier {
    val node = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)
    autofillTree += node

    return onGloballyPositioned {
        node.boundingBox = it.boundsInWindow()
    }.onFocusChanged { state ->
        autofill?.run {
            if (state.isFocused) {
                requestAutofillForNode(node)
            } else {
                cancelAutofillForNode(node)
            }
        }
    }
}
