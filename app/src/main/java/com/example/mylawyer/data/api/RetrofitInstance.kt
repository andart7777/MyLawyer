package com.example.mylawyer.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitInstance {
    private const val BASE_URL = "https://mylawyer-397230f6fd20.herokuapp.com/"

    val api: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}