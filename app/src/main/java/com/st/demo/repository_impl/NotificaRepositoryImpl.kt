package com.st.demo.repository_impl

import android.util.MalformedJsonException

import com.st.demo.api_interface.NotificationInterface
import com.st.demo.model.Notifica
import com.st.demo.wrappers.Resource

import com.unisalento.wotproject20242025potholedetect.repository.NotificaRepository
import kotlinx.serialization.SerializationException

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class NotificaRepositoryImpl @Inject constructor(
    private val notificationInterface: NotificationInterface
): NotificaRepository {
    override suspend fun getNotify(token: String): Resource<Notifica> = safeApiCall{
        notificationInterface.getNotify(token)
    }

    override suspend fun deleteNotify(
        token: String,
        notifica: Notifica
    ): Resource<Notifica> = safeApiCall{
        notificationInterface.deleteNotify(token,notifica)
    }

    override suspend fun setAdminIsLetta(
        token: String,
        id: String
    ): Resource<Notifica> = safeApiCall {
        notificationInterface.setAdminIsLetta(token,id)
    }

    override suspend fun setUserIsLetta(
        token: String,
        id: String
    ): Resource<Notifica> = safeApiCall {
        notificationInterface.setUserIsLetta(token,id)
    }

    //TODO crea classe unica da condividere con AuthRepositoryImpl
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            when {
                response.isSuccessful -> {
                    response.body()?.let { Resource.Success(it) }
                        ?: Resource.Error("HTTP 200: Empty response body")
                }
                else -> Resource.Error("HTTP Error: ${response.code()} - ${response.message()}")
            }
        } catch (exception: Throwable) {
            handleApiError(exception)
        }
    }

    // Exception handling with detailed Resource.Error
    private fun <T> handleApiError(exception: Throwable): Resource<T> {
        val message = when (exception) {
            is TimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Please check your connection."
            is HttpException -> {
                when (val statusCode = exception.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized. Please check your credentials."
                    403 -> "Forbidden. Access is denied."
                    404 -> "Resource not found."
                    500 -> "Internal Server Error. Please try again later."
                    503 -> "Service Unavailable. Please try again later."
                    else -> "Unexpected HTTP Error: $statusCode"
                }
            }
            is SerializationException, is MalformedJsonException -> "Malformed JSON received. Parsing failed."
            is IllegalArgumentException -> "Invalid argument provided. ${exception.message}"
            is IllegalStateException -> "Illegal application state. ${exception.message}"
            else -> "Unexpected error occurred: ${exception.message}"
        }
        return Resource.Error(message, exception)
    }
}