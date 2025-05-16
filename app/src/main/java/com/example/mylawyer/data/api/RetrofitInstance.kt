package com.example.mylawyer.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
//    private const val BASE_URL = "http://127.0.0.1:8000/" // 127 само устройство а не хост машина
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: ChatApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Увеличиваем таймаут подключения
            .readTimeout(30, TimeUnit.SECONDS)    // Увеличиваем таймаут чтения
            .writeTimeout(30, TimeUnit.SECONDS)   // Увеличиваем таймаут записи
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}