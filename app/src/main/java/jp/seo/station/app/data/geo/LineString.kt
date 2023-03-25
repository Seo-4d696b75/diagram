package jp.seo.station.app.data.geo

import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import kotlinx.serialization.Serializable

@Serializable
data class LineString(
    val type: String = "LineString",
    val coordinates: List<List<Double>>,
)

@Serializable
data class LineStringFeature(
    val type: String = "Feature",
    val geometry: LineString,
) {
    companion object {
        fun fromVoronoi(area: VoronoiArea) = LineStringFeature(
            geometry = LineString(
                coordinates = area.points.map { it.geoJson }
            )
        )
    }
}
