package com.kochione.kochi_one.api

import com.google.gson.JsonObject
import com.kochione.kochi_one.models.FitnessVenueSingleResponse
import com.kochione.kochi_one.models.FitnessVenuesListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface FitnessApiService {
    @GET("fitness-venues")
    suspend fun getFitnessVenues(): FitnessVenuesListResponse

    @GET("fitness-venues/{id}")
    suspend fun getFitnessVenueById(@Path("id") id: String): FitnessVenueSingleResponse

    @GET("fitness-venues/meta/category-thumbnails")
    suspend fun getFitnessCategoryThumbnails(): JsonObject
}
