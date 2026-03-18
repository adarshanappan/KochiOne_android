package com.kochione.kochi_one.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Placeholder Base URL, adjust this to your actual production/staging API server
    private const val BASE_URL = "https://admin.kochi.one/api/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val instance: ExploreApiService by lazy {
        retrofit.create(ExploreApiService::class.java)
    }

    val foodInstance: FoodApiService by lazy {
        retrofit.create(FoodApiService::class.java)
    }
}
