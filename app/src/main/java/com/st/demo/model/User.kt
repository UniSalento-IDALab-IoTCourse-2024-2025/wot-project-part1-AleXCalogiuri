package com.st.demo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User (
    @SerialName("id")
    var id:String? = null,

    @SerialName("nome")
    var nome:String? = null,

    @SerialName("cognome")
    var cognome: String? = null,

    @SerialName("email")
    var email: String? = null,

    @SerialName("role")
    var role: String? = null,

    @SerialName("password")
    var password: String? = null
)