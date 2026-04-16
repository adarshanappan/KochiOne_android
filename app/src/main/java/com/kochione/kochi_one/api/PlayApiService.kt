package com.kochione.kochi_one.api

import com.google.gson.JsonObject
import com.kochione.kochi_one.models.PlayVenuesListResponse
import retrofit2.http.GET

interface PlayApiService {
    @GET("play-venues")
    suspend fun getPlayVenues(): PlayVenuesListResponse

    @GET("play-venues/meta/category-thumbnails")
    suspend fun getPlayCategoryThumbnails(): JsonObject

//    @GET("fitness-venues/meta/category-thumbnails")
//    suspend fun getFitnessCategoryThumbnails(): JsonObject
}
