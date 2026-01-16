package com.st.demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse (
    @SerialName("jwt")
    val jwt: String? = null,
    @SerialName("role")
    val role: String? = null,

    @SerialName("status")
    val message: String?= null
)