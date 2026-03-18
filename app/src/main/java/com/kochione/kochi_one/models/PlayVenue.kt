package com.kochione.kochi_one.models

import com.google.gson.annotations.SerializedName

data class PlayVenue(
    @SerializedName("_id") val id: String,
    @SerializedName("biz_id") val bizId: String,
    val name: String,
    val description: String,
    val address: RestaurantAddress,
    val location: RestaurantLocation,
    val contact: RestaurantContact,
    val rating: Double,
    val ranking: Int,
    val playCategory: String,
    val logo: RestaurantLogo?,
    val coverImages: List<RestaurantImage>?,
    val operatingHours: OperatingHours,
    val features: List<String>,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class PlayVenuesListResponse(
    val status: String,
    val data: PlayVenuesListData
)

data class PlayVenuesListData(
    val venues: List<PlayVenue>,
    val pagination: PlayVenuesPagination
)

data class PlayVenuesPagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalVenues: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)