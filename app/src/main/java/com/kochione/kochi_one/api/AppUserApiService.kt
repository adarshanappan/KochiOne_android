package com.kochione.kochi_one.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppUserApiService {
    @GET("users/device/{deviceId}")
    suspend fun registerOrGet(@Path("deviceId") deviceId: String): AppUser

    @POST("users/device/{deviceId}/profile")
    @FormUrlEncoded
    suspend fun updateProfile(
        @Path("deviceId") deviceId: String,
        @Field("name") name: String,
        @Field("mobile") mobile: String,
        @Field("imageData") imageData: String?
    ): AppUser

    @POST("users/device/{deviceId}/push-token")
    @FormUrlEncoded
    suspend fun registerDevicePushToken(
        @Path("deviceId") deviceId: String,
        @Field("token") token: String
    ): Any
}
