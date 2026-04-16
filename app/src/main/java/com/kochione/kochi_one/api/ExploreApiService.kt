package com.kochione.kochi_one.api

import com.kochione.kochi_one.models.ExploreResponse
import retrofit2.http.GET

interface ExploreApiService {
    // Placeholder endpoint, adjust this to your actual API route
    @GET("explore")
    suspend fun getExplorePosts(): ExploreResponse
}
