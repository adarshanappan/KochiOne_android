package com.kochione.kochi_one.models

import com.google.gson.annotations.SerializedName

data class Restaurant(
    @SerializedName("_id") val id: String,
    @SerializedName("biz_id") val bizId: String,
    val name: String,
    val description: String,
    val logo: RestaurantLogo?,
    val coverImages: List<RestaurantImage>,
    val address: RestaurantAddress,
    val location: RestaurantLocation,
    val contact: RestaurantContact,
    val cuisine: List<String>,
    val features: List<String>,
    val rating: Double,
    val ranking: Int,
    val operatingHours: OperatingHours,
    val isActive: Boolean,
    val owner: String?,
    val images: List<String>,
    val restaurantType: String?,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val v: Int
)

data class RestaurantImage(
    val url: String,
    val key: String,
    val originalName: String,
    val size: Int,
    val uploadedAt: String,
    val alt: String?,
    @SerializedName("_id") val id: String?
)

data class RestaurantLogo(
    val url: String?,
    val key: String?,
    val originalName: String?,
    val size: Int?,
    val uploadedAt: String
)

data class RestaurantAddress(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

data class RestaurantLocation(
    val latitude: Double,
    val longitude: Double
)

data class RestaurantContact(
    val phone: String,
    val email: String,
    val website: String?
)

data class OperatingHours(
    val monday: DayHours,
    val tuesday: DayHours,
    val wednesday: DayHours,
    val thursday: DayHours,
    val friday: DayHours,
    val saturday: DayHours,
    val sunday: DayHours
)

data class DayHours(
    val open: String?,
    val close: String?,
    val closed: Boolean
)

data class RestaurantResponse(
    val status: String,
    val data: RestaurantData
)

data class RestaurantData(
    val restaurants: List<Restaurant>
)

data class SingleRestaurantResponse(
    val status: String,
    val data: SingleRestaurantData
)

data class SingleRestaurantData(
    val restaurant: Restaurant
)
