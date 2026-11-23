package com.englishai.app.feature.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishai.app.core.ui.AppTextField
import com.englishai.app.core.ui.ErrorText
import com.englishai.app.core.ui.PasswordField
import com.englishai.app.core.ui.PrimaryButton

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onBackToLogin: () -> Unit) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Create your account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Start learning English from day 1.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))

        AppTextField(name, { name = it }, "Full name", enabled = !form.loading)
        AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email, enabled = !form.loading)
        PasswordField(password, { password = it }, imeAction = ImeAction.Next, enabled = !form.loading)
        PasswordField(confirm, { confirm = it }, label = "Confirm password", enabled = !form.loading)
        Text("Use at least 8 characters.", style = MaterialTheme.typography.bodySmall)
        ErrorText(form.error)

        PrimaryButton("Create account", form.loading, onClick = { viewModel.register(name, email, password, confirm) })
        OrDivider()
        GoogleButton(enabled = !form.loading, viewModel = viewModel)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Already have an account?")
            TextButton(onClick = { viewModel.clearMessages(); onBackToLogin() }) { Text("Log in") }
        }
    }
}
