package jp.seo.station.app.data.geo

import jp.seo.station.app.data.Voronoi
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
        fun fromVoronoi(area: Voronoi) = LineStringFeature(
            geometry = LineString(
                coordinates = area.points.map { it.geoJson }
            )
        )
    }
}
