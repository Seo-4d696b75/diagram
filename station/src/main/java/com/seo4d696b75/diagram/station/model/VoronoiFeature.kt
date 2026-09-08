package com.seo4d696b75.diagram.station.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * GeoJSON model for [com.seo4d696b75.diagram.core.VoronoiDiagram.VoronoiArea]
 */
@Serializable
data class VoronoiFeature(
    val type: String = "Feature",
    val geometry: VoronoiGeometry,
    val properties: Map<String, JsonElement> = emptyMap(),
)

/**
 * Polygon or LineString
 */
@Serializable
sealed interface VoronoiGeometry {
    val coordinates: List<*>

    @Serializable
    @SerialName("Polygon")
    data class Polygon(
        override val coordinates: List<List<List<Double>>>,
    ) : VoronoiGeometry

    @Serializable
    @SerialName("LineString")
    data class LineString(
        override val coordinates: List<List<Double>>,
    ) : VoronoiGeometry
}
