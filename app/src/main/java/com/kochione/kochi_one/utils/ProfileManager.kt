package com.kochione.kochi_one.utils

import android.content.Context
import android.provider.Settings
import com.kochione.kochi_one.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class CountryCode(val id: String, val code: String, val name: String) {
    val display: String get() = "$name ($code)"
}

object ProfileManager {
    val countryCodes = listOf(
        CountryCode("in", "+91", "India"),
        CountryCode("us", "+1", "US"),
        CountryCode("uk", "+44", "UK"),
        CountryCode("ae", "+971", "UAE"),
        CountryCode("sa", "+966", "Saudi Arabia"),
        CountryCode("pk", "+92", "Pakistan"),
        CountryCode("bd", "+880", "Bangladesh"),
        CountryCode("sg", "+65", "Singapore"),
        CountryCode("my", "+60", "Malaysia"),
        CountryCode("au", "+61", "Australia"),
        CountryCode("de", "+49", "Germany"),
        CountryCode("fr", "+33", "France"),
        CountryCode("ca", "+1", "Canada"),
        CountryCode("other", "+", "Other")
    )

    private val _usernameFlow = MutableStateFlow("")
    val usernameFlow: StateFlow<String> = _usernameFlow.asStateFlow()

    private val _mobileFlow = MutableStateFlow("")
    val mobileFlow: StateFlow<String> = _mobileFlow.asStateFlow()

    private val _profileImageFlow = MutableStateFlow<String?>(null)
    val profileImageFlow: StateFlow<String?> = _profileImageFlow.asStateFlow()

    private val _countryCodeFlow = MutableStateFlow("+91")
    val countryCodeFlow: StateFlow<String> = _countryCodeFlow.asStateFlow()

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
        val stored = prefs.getString("profile_deviceId", null)
        if (!stored.isNullOrEmpty()) {
            return stored
        }
        var newId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (newId.isNullOrEmpty() || newId == "9774d56d682e549c") { // 9774... is a known bug on some emulators
            newId = UUID.randomUUID().toString()
        }
        prefs.edit().putString("profile_deviceId", newId).apply()
        return newId
    }

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
        _usernameFlow.value = prefs.getString("profile_username", "") ?: ""
        _mobileFlow.value = prefs.getString("profile_mobile", "") ?: ""
        _profileImageFlow.value = prefs.getString("profile_imageData", null)
        _countryCodeFlow.value = prefs.getString("profile_countryCode", "+91") ?: "+91"
    }

    fun ensureSyncedWithBackend(context: Context) {
        val deviceId = getDeviceId(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = RetrofitClient.appUserInstance.registerOrGet(deviceId)
                withContext(Dispatchers.Main) {
                    val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
                    
                    // In a real app, LikedSavedStore would merge data here. 
                    // LikedSavedStore.mergeFromServer(user)

                    val hasLocalProfile = _usernameFlow.value.isNotEmpty() || 
                                          _mobileFlow.value.isNotEmpty() || 
                                          _profileImageFlow.value != null

                    if (!hasLocalProfile) {
                        if (!user.name.isNullOrEmpty()) {
                            _usernameFlow.value = user.name
                            prefs.edit().putString("profile_username", user.name).apply()
                        }
                        if (!user.mobile.isNullOrEmpty()) {
                            _mobileFlow.value = user.mobile
                            prefs.edit().putString("profile_mobile", user.mobile).apply()
                        }
                        if (!user.profileImageUrl.isNullOrEmpty()) {
                            // If backend has a URL, we store it as our image URI
                            _profileImageFlow.value = user.profileImageUrl
                            prefs.edit().putString("profile_imageData", user.profileImageUrl).apply()
                        }
                    }
                }

                // Upload push token if exists
                val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
                val token = prefs.getString("profile_devicePushToken", null)
                if (!token.isNullOrEmpty()) {
                    try {
                        RetrofitClient.appUserInstance.registerDevicePushToken(deviceId, token)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Non-fatal: offline mode works
            }
        }
    }

    fun updateProfileImage(context: Context, newUri: String?) {
        val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("profile_imageData", newUri).apply()
        _profileImageFlow.value = newUri
    }

    fun updateAndSyncToBackend(
        context: Context,
        username: String? = null,
        mobile: String? = null,
        imageData: String? = null,
        countryCode: String? = null
    ) {
        val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        if (username != null) {
            _usernameFlow.value = username
            editor.putString("profile_username", username)
        }
        if (mobile != null) {
            _mobileFlow.value = mobile
            editor.putString("profile_mobile", mobile)
        }
        if (imageData != null) {
            _profileImageFlow.value = imageData
            editor.putString("profile_imageData", imageData)
        }
        if (countryCode != null) {
            _countryCodeFlow.value = countryCode
            editor.putString("profile_countryCode", countryCode)
        }
        editor.apply()

        val nameToSend = username ?: _usernameFlow.value
        val mobileToSend = mobile ?: _mobileFlow.value
        val imageToSend = imageData ?: _profileImageFlow.value
        val deviceId = getDeviceId(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.appUserInstance.updateProfile(deviceId, nameToSend, mobileToSend, imageToSend)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class ProfileSnapshot(
        val username: String,
        val mobile: String,
        val imageData: String?,
        val countryCode: String
    )

    fun snapshot(): ProfileSnapshot {
        return ProfileSnapshot(
            username = _usernameFlow.value,
            mobile = _mobileFlow.value,
            imageData = _profileImageFlow.value,
            countryCode = _countryCodeFlow.value
        )
    }

    fun restore(context: Context, snapshot: ProfileSnapshot) {
        val prefs = context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("profile_username", snapshot.username)
            putString("profile_mobile", snapshot.mobile)
            putString("profile_imageData", snapshot.imageData)
            putString("profile_countryCode", snapshot.countryCode)
        }.apply()

        _usernameFlow.value = snapshot.username
        _mobileFlow.value = snapshot.mobile
        _profileImageFlow.value = snapshot.imageData
        _countryCodeFlow.value = snapshot.countryCode
    }
}
