package com.englishai.app.core.network

/** Every backend response uses { success, data, error }. */
data class ApiError(val code: String?, val message: String?)

data class ApiResponse<T>(val success: Boolean, val data: T?, val error: ApiError?)
