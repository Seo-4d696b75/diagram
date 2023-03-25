package jp.seo.station.app.data.geo

import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LineString(
    val type: String = "LineString",
    val coordinates: List<List<Double>>,
)

@Serializable
data class LineStringFeature(
    val type: String = "Feature",
    val geometry: LineString,
    val properties: Map<String, JsonElement> = emptyMap(),
) {
    companion object {
        fun fromVoronoi(area: VoronoiArea) = LineStringFeature(
            geometry = LineString(
                coordinates = area.points.map { it.geoJson }
            )
        )
    }
}
