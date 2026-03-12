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
    val mediaUrl: String,
    val mediaType: String,
    val logoUrl: String,
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val detailBody: String,
    val redirectUrl: String,
    val buttonLabel: String,
    val accentColor: String
)

data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalPosts: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)
