package com.st.demo.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Serializable
data class Road @OptIn(ExperimentalTime::class) constructor(
    val stradaId: String,
    val nomeStrada: String? = null,
    val tipologia: List<String>? = null,
    val via: String,
    val citta: String,
    val provincia: String,
    val regione: String,
    val cap: String?="",
    val gestoreId: String? = null,
    val lunghezzaTotale: Double? = null,
    val superficiePredominante: String? = null,
    val ultimaManutenzione: Instant? = null,
    val statoConservazione: Int? = null,
    val segmenti: List<SegmentDTO>? = null,
    val creatoIl: Instant,
    val ultimaModifica: Instant
) {
    @Serializable
    data class SegmentDTO(
        val segmentId: String,
        val inizio: GeoJsonPoint?,
        val fine: GeoJsonPoint? = null,
        val lunghezza: Double? = null,
        val prioritaManutenzione: Int? = 1,
        val superficie: String? = null,
        val risultatoSegnalazione: String? = null
    )
}

@Serializable
data class GeoJsonPoint(
    val x: Double,
    val y: Double,
    val type: String,
    val coordinates: List<Double>
)