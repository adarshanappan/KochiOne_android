package com.kochione.kochi_one.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kochione.kochi_one.api.RetrofitClient
import com.kochione.kochi_one.models.ExplorePost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<ExplorePost>>(emptyList())
    
    val posts: StateFlow<List<ExplorePost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Call API
                val response = RetrofitClient.instance.getExplorePosts()
                if (response.status == "success") {
                    _posts.value = response.data.posts
                } else {
                    _errorMessage.value = "Failed to load posts"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    }

