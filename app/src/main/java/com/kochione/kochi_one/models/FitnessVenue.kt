package com.kochione.kochi_one.models

import com.google.gson.annotations.SerializedName

data class FitnessVenue(
    @SerializedName("_id") val id: String,
    @SerializedName("biz_id") val bizId: String,
    val name: String,
    val description: String,
    val address: RestaurantAddress,
    val location: RestaurantLocation,
    val contact: RestaurantContact,
    val rating: Double,
    val ranking: Int,
    @SerializedName("fitness_category") val fitnessCategory: String,
    val logo: RestaurantLogo?,
    @SerializedName("cover_images") val coverImages: List<RestaurantImage>?,
    @SerializedName("operating_hours") val operatingHours: OperatingHours,
    val features: List<String>,
//    val updates: List<RestaurantUpdate>?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class FitnessVenuesListResponse(
    val status: String,
    val data: FitnessVenuesListData
)

data class FitnessVenuesListData(
    val venues: List<FitnessVenue>,
    val pagination: FitnessVenuesPagination
)

data class FitnessVenuesPagination(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_venues") val totalVenues: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean
)

data class FitnessVenueSingleResponse(
    val status: String,
    val data: FitnessVenueSingleData
)

data class FitnessVenueSingleData(
    val venue: FitnessVenue
)

//data class RestaurantUpdate(
//    @SerializedName("_id") val id: String? = null,
//    val title: String? = null,
//    val description: String? = null,
//    val image: RestaurantImage? = null,
//    val createdAt: String? = null,
//    val updatedAt: String? = null
//)