package com.kochione.kochi_one.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class SavedSection {
    EATS,
    PLAY,
    FITNESS
}

enum class SavedBucket {
    LIKED,
    SAVED
}

data class SavedItem(
    val id: String,
    val section: SavedSection,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String,
    val logoUrl: String? = null,
    val galleryImages: List<String>? = null,
    val distanceLabel: String? = null,
    val statusLabel: String? = null,
    val statusSuffix: String? = null
)

object LikedSavedStore {
    private const val PREFS_NAME = "liked_saved_store"
    private const val LIKED_KEY = "liked_items"
    private const val SAVED_KEY = "saved_items"
    private val gson = Gson()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun keyFor(item: SavedItem): String = "${item.section}:${item.id}"

    private fun readMap(context: Context, bucket: SavedBucket): MutableMap<String, SavedItem> {
        val key = if (bucket == SavedBucket.LIKED) LIKED_KEY else SAVED_KEY
        val raw = prefs(context).getString(key, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, SavedItem>>() {}.type
        return gson.fromJson(raw, type) ?: mutableMapOf()
    }

    private fun writeMap(context: Context, bucket: SavedBucket, map: Map<String, SavedItem>) {
        val key = if (bucket == SavedBucket.LIKED) LIKED_KEY else SAVED_KEY
        prefs(context).edit().putString(key, gson.toJson(map)).apply()
    }

    fun isInBucket(context: Context, bucket: SavedBucket, section: SavedSection, id: String): Boolean {
        val map = readMap(context, bucket)
        return map.containsKey("${section}:${id}")
    }

    fun setInBucket(context: Context, bucket: SavedBucket, item: SavedItem, enabled: Boolean) {
        val map = readMap(context, bucket)
        val key = keyFor(item)
        if (enabled) {
            map[key] = item
        } else {
            map.remove(key)
        }
        writeMap(context, bucket, map)
    }

    fun all(context: Context, bucket: SavedBucket): List<SavedItem> {
        return readMap(context, bucket).values.toList()
    }
}
