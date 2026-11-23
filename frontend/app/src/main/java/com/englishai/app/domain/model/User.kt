package com.englishai.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val learningLevel: String?
)

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val message: String, val code: String? = null) : AppResult<Nothing>
}
