package com.kochione.kochi_one.api

import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Placeholder Base URL, adjust this to your actual production/staging API server
    private const val BASE_URL = "https://admin.kochi.one/api/"

    /** One client for all services: connection reuse + HTTP/2 to same host (faster than new handshakes per call). */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
            .build()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val instance: ExploreApiService by lazy {
        retrofit.create(ExploreApiService::class.java)
    }

    val foodInstance: FoodApiService by lazy {
        retrofit.create(FoodApiService::class.java)
    }

    val playInstance: PlayApiService by lazy {
        retrofit.create(PlayApiService::class.java)
    }

    val fitnessInstance: FitnessApiService by lazy {
        retrofit.create(FitnessApiService::class.java)
    }
}
