package com.kochione.kochi_one.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.kochione.kochi_one.api.RetrofitClient
import com.kochione.kochi_one.models.PlayVenue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class PlayViewModel : ViewModel() {
    private companion object {
        const val API_ORIGIN = "https://api.kochi.one"
        const val MIN_LOADING_MS = 900L
        const val MIN_ERROR_LOADING_MS = 7000L
    }

    private val _venues = MutableStateFlow<List<PlayVenue>>(emptyList())
    val venues: StateFlow<List<PlayVenue>> = _venues.asStateFlow()

    private val _categoryThumbnails = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryThumbnails: StateFlow<Map<String, String>> = _categoryThumbnails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchPlayVenues()
    }

    fun fetchPlayVenues() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var hasError = false
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Run both API calls in parallel (sequential was ~sum of latencies; iOS typically parallelizes).
                supervisorScope {
                    val venuesDeferred = async(Dispatchers.IO) { RetrofitClient.playInstance.getPlayVenues() }
                    val thumbsDeferred = async(Dispatchers.IO) { RetrofitClient.playInstance.getPlayCategoryThumbnails() }

                    val response = try {
                        venuesDeferred.await()
                    } catch (e: Exception) {
                        hasError = true
                        _errorMessage.value = "Network error: ${e.localizedMessage}"
                        return@supervisorScope
                    }

                    if (response.status == "success") {
                        _venues.value = response.data.venues
                    } else {
                        hasError = true
                        _errorMessage.value = "Failed to load play venues"
                    }

                    try {
                        val thumbnails = thumbsDeferred.await()
                        // Parse off main — large meta JSON can hitch the UI; avoid ?cb= on URLs so Coil can disk-cache.
                        _categoryThumbnails.value = withContext(Dispatchers.Default) {
                            parseCategoryThumbnailUrls(thumbnails)
                                .mapValues { (_, rawUrl) -> normalizeImageUrl(rawUrl) }
                        }
                    } catch (_: Exception) {
                        // Venues already shown; optional category images can stay empty / use local fallbacks.
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                val elapsed = System.currentTimeMillis() - startTime
                val targetMinLoading = if (hasError) MIN_ERROR_LOADING_MS else MIN_LOADING_MS
                if (elapsed < targetMinLoading) {
                    delay(targetMinLoading - elapsed)
                }
                _isLoading.value = false
            }
        }
    }

    private fun parseCategoryThumbnailUrls(root: JsonObject): Map<String, String> {
        val results = mutableMapOf<String, String>()

        fun add(category: String?, url: String?) {
            if (category.isNullOrBlank() || url.isNullOrBlank()) return
            results[category.trim().lowercase()] = url.trim()
        }

        fun getString(obj: JsonObject, vararg names: String): String? {
            return names.firstNotNullOfOrNull { key ->
                obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString
            }
        }

        fun urlFromElement(element: JsonElement?): String? {
            if (element == null || element.isJsonNull) return null
            if (element.isJsonPrimitive) {
                return element.asString.takeIf { it.isNotBlank() }
            }
            if (element.isJsonObject) {
                val obj = element.asJsonObject
                return getString(
                    obj,
                    "url",
                    "thumbnail",
                    "thumbnailUrl",
                    "image",
                    "imageUrl",
                    "coverImage",
                    "coverImageUrl"
                )
            }
            return null
        }

        fun walk(element: JsonElement?) {
            if (element == null || element.isJsonNull) return
            when {
                element.isJsonArray -> {
                    element.asJsonArray.forEach { walk(it) }
                }
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    val category = getString(obj, "category", "categoryName", "name", "slug", "type")
                    val directUrl = getString(
                        obj,
                        "url",
                        "thumbnail",
                        "thumbnailUrl",
                        "image",
                        "imageUrl",
                        "coverImage",
                        "coverImageUrl"
                    ) ?: urlFromElement(obj.get("thumbnail"))
                    ?: urlFromElement(obj.get("image"))
                    ?: urlFromElement(obj.get("coverImage"))
                    add(category, directUrl)

                    obj.entrySet().forEach { (key, value) ->
                        if (value.isJsonPrimitive) {
                            val valueAsUrl = urlFromElement(value)
                            if (valueAsUrl != null) add(key, valueAsUrl)
                        } else if (value.isJsonObject) {
                            add(key, urlFromElement(value))
                        }
                        walk(value)
                    }
                }
            }
        }

        walk(root)
        return results
    }

    private fun normalizeImageUrl(url: String): String {
        if (url.isBlank()) return url
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "$API_ORIGIN$url"
            else -> "$API_ORIGIN/$url"
        }
    }
}