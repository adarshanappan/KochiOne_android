package com.kochione.kochi_one.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiManager {
    // ⚠️ Security Note: In a production environment, never hardcode API keys.
    // They should ideally be fetched from a secure backend or injected via BuildConfig.
    private const val OPENAI_API_KEY = ""
    private const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"

    private val SYSTEM_CONTEXT = """
    Your name is Lilly, an AI assistant inside the Kochi One app. Your job is to help people understand and use the app clearly, step by step, in a friendly concise way.

    Kochi One app overview:
    - The app opens with onboarding, then location permission, then notification permission, then the main map with a bottom sheet.
    - The main experience is a map in the background and a draggable bottom sheet in front.
    - The bottom sheet has these main tabs: Explore, Eats, Play, Fitness, Transit.
    - Users can open chat from the message capsule in the header.
    - Users can open profile from the profile circle in the header.

    Feature guide:
    - Explore: editorial-style cards about happenings and featured places in Kochi. Users can tap a card to open a detailed story view, read the full content, browse gallery media, and use the action button to open the destination link.
    - Eats: discover cafes, restaurants, food spots, pubs, bars, and similar venues. Users can browse cards, open details, check timings, images, links, and like or save places.
    - Play: discover sports, gaming, snooker, cricket turf, soccer turf, badminton, fun activities, and related venues. Users can browse categories, open details, and explore activity options.
    - Fitness: discover gyms, yoga, MMA, fitness centres, studios, and health clubs. Users can browse categories, open details, and review venue information.
    - Transit: users can explore Kochi Metro route information, station flow, fare-related information, train movement, and QR / transit actions available in the app.

    Profile and support:
    - Profile includes edit profile, liked items, saved items, report a problem, help, check for update, notification settings, appearance, and about.
    - Appearance lets users switch between Light, Dark, or Automatic.
    - About includes Instagram, website, email, and enquiry form.
    - Report a problem lets users submit issues from inside the app.

    How to answer:
    - When users ask how to use the app, explain the flow in practical steps.
    - Prefer short step-by-step guidance over long paragraphs.
    - Mention the exact tab or button name when possible.
    - If the user sounds unsure, suggest the most relevant tab or action first.
    - If the question is about discovering places, direct them to Explore / Eats / Play / Fitness depending on intent.
    - If the question is about metro or train flow, direct them to Transit.
    - If the question is about account, settings, likes, saves, updates, appearance, or enquiry, direct them to Profile.
    - If information is not clearly available in the app context, say so briefly instead of inventing details.

    Response formatting rules:
    - Always format your replies using this custom marker system instead of Markdown, HTML, or plain bullet formatting.
    - Use only these markers:
      (1) main title
      (2) section heading
      (3) subheading
      (4) paragraph
      (5) bullet point
    - Every marker must be closed with the same marker number.
    - CRITICAL: You MUST wrap EACH individual step in its own (5)...(5) markers. Never combine multiple steps into a single (5) block!
    - Do not mix markers on one line.
    - Do not output Markdown headings, asterisks, dashes, or numbered lists unless they are inside normal paragraph text.
    - Keep paragraphs reasonably short and readable.
    - For usage/help answers, prefer this layout:
      (1)Short Title(1)
      (4)Short intro paragraph.(4)
      (2)Steps(2)
      (5)First step(5)
      (5)Second step(5)
      (5)Third step(5)
      (2)Extra Help(2)
      (4)Short closing help paragraph.(4)
    - If the user asks a very small direct question, you can still answer briefly, but the answer must remain inside the same marker system.
    """.trimIndent()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getSuggestionsForTab(tabName: String): List<String>? = withContext(Dispatchers.IO) {
        val topicContext = when (tabName) {
            "Food" -> "restaurants, local food, cafes, and dining"
            "Play" -> "entertainment, activities, games, and beaches"
            "Fitness" -> "gyms, running tracks, and fitness centers"
            "Transit" -> "metro lines, buses, and public transport"
            else -> "food, fitness, entertainment, beaches, cafes, and transit"
        }

        val prompt = "Generate 8 short search suggestions (2-4 words each) for a Kochi city guide app specifically focusing on $topicContext. Respond with ONLY a raw JSON array of strings like: [\"suggestion1\",\"suggestion2\"]"

        val reqJson = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful assistant. Always respond with valid JSON only, no markdown, no code fences.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1024)
            put("temperature", 0.3) // Lower temperature for more consistent results across platforms
            put("response_format", JSONObject().apply { put("type", "json_object") })
        }

        val body = reqJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(OPENAI_URL)
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: return@use null
                    val respJson = JSONObject(rawBody)
                    val content = respJson
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                    
                    val cleaned = content
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val arr: JSONArray? = try {
                        if (cleaned.startsWith("{")) {
                            val obj = JSONObject(cleaned)
                            // Grab the first array value found
                            obj.keys().asSequence()
                                .mapNotNull { key -> runCatching { obj.getJSONArray(key) }.getOrNull() }
                                .firstOrNull()
                        } else {
                            JSONArray(cleaned)
                        }
                    } catch (_: Exception) {
                        val match = Regex("\\[[^\\[\\]]+\\]").find(cleaned)
                        match?.let { runCatching { JSONArray(it.value) }.getOrNull() }
                    }

                    if (arr != null) {
                        val result = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            result.add(arr.getString(i))
                        }
                        result
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

//    fun getFallbackSuggestionsForTab(tabName: String): List<String> {
//        return when (tabName) {
//            "Food" -> listOf("Best breakfast spots", "Biryani near me", "Top cafes", "Seafood restaurants", "Late night food", "Street food", "Vegan options", "Buffet deals")
//            "Play" -> listOf("Bowling alleys", "Arcades", "Turf bookings", "Movies tonight", "Fort Kochi walks", "Wonderla tickets", "Parks near me", "Live music")
//            "Fitness" -> listOf("Gyms near me", "Zumba classes", "Yoga studios", "Crossfit boxes", "Running tracks", "Swimming pools", "Badminton courts", "Personal trainers")
//            "Transit" -> listOf("Metro timings", "Bus routes", "Water metro tickets", "Taxi stands", "Airport shuttle", "Auto rickshaws", "Train schedule", "Ferry timings")
//            else -> listOf("Best breakfast spots", "Metro timings", "Hidden gems in Kochi", "Gyms near me", "Things to do tonight", "Fort Kochi walks", "Best cafes", "Backwater cruises")
//        }
//    }

    suspend fun requestChat(conversation: List<com.kochione.kochi_one.views.ChatMessage>): String? = withContext(Dispatchers.IO) {
        val reqJson = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_CONTEXT)
                })
                for (msg in conversation) {
                    put(JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "assistant")
                        put("content", msg.text)
                    })
                }
            })
            put("max_tokens", 1024)
            put("temperature", 0.3)
        }

        val body = reqJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(OPENAI_URL)
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: return@use null
                    val respJson = JSONObject(rawBody)
                    respJson
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
