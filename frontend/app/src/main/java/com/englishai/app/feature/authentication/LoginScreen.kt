package com.englishai.app.feature.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishai.app.core.ui.AppTextField
import com.englishai.app.core.ui.ErrorText
import com.englishai.app.core.ui.PasswordField
import com.englishai.app.core.ui.PrimaryButton
import kotlinx.coroutines.launch

@Composable
fun GoogleButton(enabled: Boolean, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            viewModel.clearMessages()
            scope.launch {
                try {
                    GoogleSignInHelper.getIdToken(context)?.let { viewModel.signInWithGoogle(it) }
                } catch (e: Exception) {
                    viewModel.showError(e.message ?: "Google sign-in failed.")
                }
            }
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) { Text("Continue with Google") }
}

@Composable
fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text("or", style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Welcome back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Log in to continue your 60-day English course.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))

        AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email, enabled = !form.loading)
        PasswordField(password, { password = it }, enabled = !form.loading)
        ErrorText(form.error)

        TextButton(onClick = { viewModel.clearMessages(); onForgotPassword() }, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?")
        }
        PrimaryButton("Log in", form.loading, onClick = { viewModel.login(email, password) })
        OrDivider()
        GoogleButton(enabled = !form.loading, viewModel = viewModel)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("New to English AI?")
            TextButton(onClick = { viewModel.clearMessages(); onRegister() }) { Text("Create account") }
        }
    }
}
