package com.englishai.app.data.repository

import com.englishai.app.core.network.ApiResponse
import com.englishai.app.data.remote.AuthApi
import com.englishai.app.data.remote.RegisterRequest
import com.englishai.app.data.remote.UserEnvelope
import com.englishai.app.domain.model.AppResult
import com.englishai.app.domain.model.User
import com.englishai.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val api: AuthApi,
    private val gson: Gson
) : AuthRepository {

    override val hasFirebaseSession: Boolean get() = auth.currentUser != null

    override suspend fun register(name: String, email: String, password: String): AppResult<User> = guarded {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
        result.user?.getIdToken(true)?.await() // refresh so the token carries the name
        api.register(RegisterRequest(name)).toUser()
    }

    override suspend fun login(email: String, password: String): AppResult<User> = guarded {
        auth.signInWithEmailAndPassword(email, password).await()
        api.login().toUser()
    }

    override suspend fun signInWithGoogle(googleIdToken: String): AppResult<User> = guarded {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        auth.signInWithCredential(credential).await()
        api.google().toUser()
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> = guarded {
        auth.sendPasswordResetEmail(email).await()
        AppResult.Success(Unit)
    }

    override suspend fun restoreSession(): AppResult<User> {
        if (auth.currentUser == null) return AppResult.Failure("Not signed in", "NO_SESSION")
        val result = guarded { api.me().toUser() }
        // Only drop the Firebase session if the backend rejected it; keep it on network errors.
        if (result is AppResult.Failure && result.code in setOf("UNAUTHORIZED", "ACCOUNT_DISABLED", "USER_NOT_FOUND")) {
            auth.signOut()
        }
        return result
    }

    override suspend fun logout() {
        runCatching { api.logout() } // best effort; never block sign-out on the network
        auth.signOut()
    }

    override suspend fun deleteAccount(): AppResult<Unit> = guarded {
        api.deleteAccount()
        auth.signOut()
        AppResult.Success(Unit)
    }

    // ---------- helpers ----------

    private fun ApiResponse<UserEnvelope>.toUser(): AppResult<User> {
        val u = data?.user
        return if (success && u != null) {
            AppResult.Success(User(u.id, u.name, u.email, u.photoUrl, u.role, u.learningLevel))
        } else {
            AppResult.Failure(error?.message ?: "Something went wrong", error?.code)
        }
    }

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = try {
        block()
    } catch (e: HttpException) {
        val body = runCatching {
            gson.fromJson(e.response()?.errorBody()?.string(), ApiResponse::class.java)
        }.getOrNull()
        AppResult.Failure(body?.error?.message ?: "Server error (${e.code()})", body?.error?.code)
    } catch (e: FirebaseAuthWeakPasswordException) {
        AppResult.Failure("Password is too weak. Use at least 8 characters.")
    } catch (e: FirebaseAuthUserCollisionException) {
        AppResult.Failure("An account with this email already exists. Try logging in.")
    } catch (e: FirebaseAuthInvalidUserException) {
        AppResult.Failure("No account found for this email.")
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        AppResult.Failure("Incorrect email or password.")
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        AppResult.Failure("Please log in again, then retry.")
    } catch (e: FirebaseNetworkException) {
        AppResult.Failure("No internet connection.", "NETWORK")
    } catch (e: IOException) {
        AppResult.Failure("Could not reach the server. Check your connection.", "NETWORK")
    } catch (e: Exception) {
        AppResult.Failure(e.message ?: "Something went wrong")
    }
}
