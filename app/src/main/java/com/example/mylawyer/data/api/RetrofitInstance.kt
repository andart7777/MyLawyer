package com.example.mylawyer.data.api

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    //    private const val BASE_URL = "http://127.0.0.1:8000/" // 127 само устройство а не хост машина
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private var token: String? = null

    private fun updateToken(): String? {
        return runBlocking {
            try {
                val user = Firebase.auth.currentUser
                if (user != null) {
                    val result = user.getIdToken(true).await() // Принудительно обновляем токен
                    token = result.token
                    Log.d("RetrofitInstance", "Токен обновлён: $token")
                    token
                } else {
                    Log.e("RetrofitInstance", "Пользователь не авторизован")
                    null
                }
            } catch (e: Exception) {
                Log.e("RetrofitInstance", "Ошибка обновления токена: ${e.message}", e)
                null
            }
        }
    }

    private val authInterceptor = Interceptor { chain ->
        // Обновляем токен перед каждым запросом
        val currentToken = updateToken() ?: run {
            Log.w("RetrofitInstance", "Токен отсутствует, отправка без авторизации")
            return@Interceptor chain.proceed(chain.request())
        }

        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $currentToken")
            .build()

        val response = chain.proceed(newRequest)
        Log.d("RetrofitInstance", "Ответ сервера: ${response.code} ${response.message}")
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "No response body"
            Log.e("RetrofitInstance", "Ошибка сервера: ${response.code}, тело: $errorBody")
        }

        // Повторяем запрос при 401
        if (response.code == 401) {
            Log.d("RetrofitInstance", "Получен 401, повторное обновление токена")
            val newToken = updateToken()
            newToken?.let {
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $it")
                    .build()
                chain.proceed(retryRequest)
            } ?: response
        } else {
            response
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ChatApi::class.java)
    }

    fun setToken(newToken: String) {
        token = newToken
        Log.d("RetrofitInstance", "Токен установлен: $newToken")
    }
}