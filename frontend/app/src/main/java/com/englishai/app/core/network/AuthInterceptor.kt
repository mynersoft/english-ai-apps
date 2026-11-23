package com.englishai.app.core.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Adds the current Firebase ID token to every request. The backend verifies it. */
class AuthInterceptor @Inject constructor(private val auth: FirebaseAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder().header("Accept", "application/json")
        val user = auth.currentUser
        if (user != null) {
            // OkHttp interceptors run on a background thread, so blocking here is safe.
            val token = runCatching { Tasks.await(user.getIdToken(false)).token }.getOrNull()
            if (token != null) builder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
