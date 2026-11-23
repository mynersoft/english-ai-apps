package com.englishai.app.data.remote

import com.englishai.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val status: String,
    val learningLevel: String?
)

data class UserEnvelope(val user: UserDto, val isNewUser: Boolean? = null)
data class RegisterRequest(val name: String)

/** Token is sent in the Authorization header by AuthInterceptor. */
interface AuthApi {
    @POST("api/v1/auth/google") suspend fun google(): ApiResponse<UserEnvelope>
    @POST("api/v1/auth/email/register") suspend fun register(@Body body: RegisterRequest): ApiResponse<UserEnvelope>
    @POST("api/v1/auth/email/login") suspend fun login(): ApiResponse<UserEnvelope>
    @POST("api/v1/auth/logout") suspend fun logout(): ApiResponse<Any?>
    @GET("api/v1/auth/me") suspend fun me(): ApiResponse<UserEnvelope>
    @DELETE("api/v1/auth/account") suspend fun deleteAccount(): ApiResponse<Any?>
}
