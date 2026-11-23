package com.englishai.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.englishai.app.domain.model.User

/** Placeholder until Phase 3 (Learning System). */
@Composable
fun HomeScreen(user: User, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hello, ${user.name}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(user.email, style = MaterialTheme.typography.bodyMedium)
        Text("Your Day 1 lesson will appear here.", style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onLogout) { Text("Log out") }
    }
}
