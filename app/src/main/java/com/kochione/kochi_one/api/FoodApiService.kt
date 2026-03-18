package com.kochione.kochi_one.api

import com.kochione.kochi_one.models.RestaurantResponse
import com.kochione.kochi_one.models.SingleRestaurantResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FoodApiService {
    @GET("restaurants")
    suspend fun getRestaurants(): RestaurantResponse

    @GET("restaurants/biz/{bizId}")
    suspend fun getRestaurantByBizId(@Path("bizId") bizId: String): SingleRestaurantResponse
}
