package com.englishai.app.feature.authentication

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishai.app.domain.model.AppResult
import com.englishai.app.domain.model.User
import com.englishai.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

data class AuthFormState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {

    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    val session: StateFlow<SessionState> = _session.asStateFlow()

    private val _form = MutableStateFlow(AuthFormState())
    val form: StateFlow<AuthFormState> = _form.asStateFlow()

    init { restore() }

    private fun restore() {
        viewModelScope.launch {
            if (!repo.hasFirebaseSession) {
                _session.value = SessionState.LoggedOut
                return@launch
            }
            when (val r = repo.restoreSession()) {
                is AppResult.Success -> _session.value = SessionState.LoggedIn(r.value)
                is AppResult.Failure -> {
                    _session.value = SessionState.LoggedOut
                    if (r.code == "NETWORK") _form.update { it.copy(error = r.message) }
                }
            }
        }
    }

    fun clearMessages() = _form.update { it.copy(error = null, info = null) }

    fun showError(message: String) = _form.update { it.copy(error = message, loading = false) }

    fun login(email: String, password: String) {
        val e = email.trim()
        validateEmail(e)?.let { showError(it); return }
        if (password.isEmpty()) { showError("Enter your password."); return }
        authenticate { repo.login(e, password) }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        val n = name.trim()
        val e = email.trim()
        if (n.length < 2) { showError("Enter your full name."); return }
        validateEmail(e)?.let { showError(it); return }
        if (password.length < 8) { showError("Password must be at least 8 characters."); return }
        if (password != confirm) { showError("Passwords do not match."); return }
        authenticate { repo.register(n, e, password) }
    }

    fun signInWithGoogle(idToken: String) {
        authenticate { repo.signInWithGoogle(idToken) }
    }

    fun sendPasswordReset(email: String) {
        val e = email.trim()
        validateEmail(e)?.let { showError(it); return }
        viewModelScope.launch {
            _form.value = AuthFormState(loading = true)
            _form.value = when (val r = repo.sendPasswordReset(e)) {
                is AppResult.Success -> AuthFormState(info = "If an account exists for $e, a reset link has been sent.")
                is AppResult.Failure -> AuthFormState(error = r.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _form.value = AuthFormState()
            _session.value = SessionState.LoggedOut
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            when (val r = repo.deleteAccount()) {
                is AppResult.Success -> { _form.value = AuthFormState(); _session.value = SessionState.LoggedOut }
                is AppResult.Failure -> _form.update { it.copy(error = r.message) }
            }
        }
    }

    private fun authenticate(block: suspend () -> AppResult<User>) {
        viewModelScope.launch {
            _form.value = AuthFormState(loading = true)
            when (val r = block()) {
                is AppResult.Success -> { _form.value = AuthFormState(); _session.value = SessionState.LoggedIn(r.value) }
                is AppResult.Failure -> _form.value = AuthFormState(error = r.message)
            }
        }
    }

    private fun validateEmail(email: String): String? = when {
        email.isEmpty() -> "Enter your email."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
        else -> null
    }
}
