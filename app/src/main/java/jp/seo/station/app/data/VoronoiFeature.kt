package jp.seo.station.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * GeoJSON model for [jp.seo.diagram.core.VoronoiDiagram.VoronoiArea]
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