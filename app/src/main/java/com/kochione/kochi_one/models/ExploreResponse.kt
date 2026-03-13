package com.kochione.kochi_one.models

data class ExploreResponse(
    val status: String,
    val data: ExploreData
)

data class ExploreData(
    val posts: List<ExplorePost>,
    val pagination: Pagination
)

data class ExplorePost(
    val id: String,
    val accountId: String,
    val accountName: String,
    val accountLogoUrl: String,
    val eyebrow: String,
    val title: String,
    val description: String,
    val detailBody: String,
    val bannerImageUrl: String,
    val bannerImageType: String,
    val galleryImages: List<GalleryImage>,
    val redirectUrl: String,
    val buttonLabel: String
)

data class GalleryImage(
    val id: String,
    val url: String,
    val type: String
)

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalPosts: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)
