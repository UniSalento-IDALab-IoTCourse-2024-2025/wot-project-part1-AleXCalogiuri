package com.st.demo.wrappers

sealed class Resource<out T> {
    // Represents a loading state
    data object Loading : Resource<Nothing>()

    // Represents a successful result with data
    data class Success<T>(val data: T) : Resource<T>()

    // Represents an error state with a message and optional exception
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : Resource<Nothing>()
}