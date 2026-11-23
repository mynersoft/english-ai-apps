package com.englishai.app.domain.repository

import com.englishai.app.domain.model.AppResult
import com.englishai.app.domain.model.User

interface AuthRepository {
    val hasFirebaseSession: Boolean
    suspend fun register(name: String, email: String, password: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun signInWithGoogle(googleIdToken: String): AppResult<User>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
    suspend fun restoreSession(): AppResult<User>
    suspend fun logout()
    suspend fun deleteAccount(): AppResult<Unit>
}
