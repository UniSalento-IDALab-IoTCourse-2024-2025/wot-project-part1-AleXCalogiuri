package com.st.demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(
    @SerialName("jwt")
    val jwt: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("message")
    val message: String? = null
) {
    companion object {
        const val BAD_CREDENTIALS = "Bad credentials"
        const val LOGIN_OK = "ok"
    }
}
