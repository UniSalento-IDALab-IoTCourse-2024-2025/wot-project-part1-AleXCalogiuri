package com.st.demo.model

import kotlinx.serialization.SerialName
import java.time.LocalDateTime

data class Notifica (
    @SerialName("id")
    var  id: String?,

    @SerialName("id_mittente")
    var idMittente: String?,

    @SerialName("type")
    var type: String?,

    @SerialName("message")
    var message: String?,

    @SerialName("letta")
    var letta: Boolean,

    @SerialName("data")
    var data: LocalDateTime
)