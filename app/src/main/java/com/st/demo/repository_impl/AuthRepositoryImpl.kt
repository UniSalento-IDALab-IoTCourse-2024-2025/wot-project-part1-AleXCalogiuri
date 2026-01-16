package com.st.demo.repository_impl

import android.util.MalformedJsonException

import com.st.demo.api_interface.AuthService
import com.st.demo.model.LoginResponse
import com.st.demo.model.LoginUser
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.User
import com.st.demo.repository.AuthRepository
import com.st.demo.wrappers.Resource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException
import javax.inject.Inject


//The repository handles network calls through
// encapsulating error handling within each function to provide a clean API for the Use Cases.
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(loginUser: LoginUser): Resource<LoginResponse> = safeApiCall {
        authService.login(loginUser)
    }

    override suspend fun signup(user: User): Resource<RegistrationResponse> = safeApiCall {
        authService.registration(user)
    }

    override suspend fun getUserByEmail(
        token: String,
        email: String
    ): Resource<User> = safeApiCall{
        authService.getByEmail(token,email)
    }

    // Safe API call handler with suspend
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
