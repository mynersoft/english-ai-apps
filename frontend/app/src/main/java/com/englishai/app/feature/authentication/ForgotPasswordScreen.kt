package com.englishai.app.feature.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishai.app.core.ui.AppTextField
import com.englishai.app.core.ui.ErrorText
import com.englishai.app.core.ui.PrimaryButton

@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Reset your password", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Enter your email and we will send you a reset link.", style = MaterialTheme.typography.bodyLarge)
        AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email, enabled = !form.loading)
        ErrorText(form.error)
        form.info?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        PrimaryButton("Send reset link", form.loading, onClick = { viewModel.sendPasswordReset(email) })
        TextButton(onClick = { viewModel.clearMessages(); onBack() }) { Text("Back to log in") }
    }
}
