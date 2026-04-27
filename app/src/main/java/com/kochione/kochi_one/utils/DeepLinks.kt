package com.kochione.kochi_one.utils

import android.net.Uri

enum class KochiLinkType(val value: String) {
    FOOD("food"),
    PLAY("play"),
    FITNESS("fitness")
}

/**
 * Builds a shareable deep link URL.
 *
 * Note: This is a URL included in share text. To open it inside the app,
 * you also need an Android Manifest intent-filter + intent routing.
 */
fun buildKochiDeepLink(
    type: KochiLinkType,
    bizId: String
): String {
    val safeBizId = bizId.trim()
    return Uri.Builder()
        .scheme("https")
        .authority("kochi.one")
        .appendPath("open")
        .appendQueryParameter("type", type.value)
        .appendQueryParameter("biz_id", safeBizId)
        .build()
        .toString()
}

