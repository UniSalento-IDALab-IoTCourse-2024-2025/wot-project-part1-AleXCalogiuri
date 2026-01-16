package com.st.demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(
    @SerialName("email")
    var email:String? = null,

    @SerialName("password")
    var password:String? = null
)
