package com.kochione.kochi_one.api

import com.kochione.kochi_one.models.RestaurantResponse
import retrofit2.http.GET

interface FoodApiService {
    @GET("restaurants")
    suspend fun getRestaurants(): RestaurantResponse
}
