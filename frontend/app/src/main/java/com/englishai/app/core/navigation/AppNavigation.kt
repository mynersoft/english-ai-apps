package com.englishai.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.englishai.app.feature.authentication.AuthViewModel
import com.englishai.app.feature.authentication.ForgotPasswordScreen
import com.englishai.app.feature.authentication.LoginScreen
import com.englishai.app.feature.authentication.RegisterScreen
import com.englishai.app.feature.authentication.SessionState
import com.englishai.app.feature.home.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
}

/** Session decides the graph: loading -> splash, logged in -> home, logged out -> auth screens. */
@Composable
fun AppRoot(viewModel: AuthViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val s = session) {
            SessionState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is SessionState.LoggedIn -> HomeScreen(user = s.user, onLogout = { viewModel.logout() })
            SessionState.LoggedOut -> AuthNavHost(viewModel)
        }
    }
}

@Composable
private fun AuthNavHost(viewModel: AuthViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onRegister = { nav.navigate(Routes.REGISTER) },
                onForgotPassword = { nav.navigate(Routes.FORGOT) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(viewModel = viewModel, onBackToLogin = { nav.popBackStack() })
        }
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(viewModel = viewModel, onBack = { nav.popBackStack() })
        }
    }
}
