package jp.seo.station.app.data.geo

import jp.seo.station.app.data.Voronoi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Polygon(
    val type: String = "Polygon",
    val coordinates: List<List<List<Double>>>,
)

@Serializable
data class PolygonFeature(
    val type: String = "Feature",
    val geometry: Polygon,
    val properties: Map<String, JsonElement> = emptyMap(),
) {
    companion object {
        fun fromVoronoi(are: Voronoi) = PolygonFeature(
            geometry = Polygon(
                coordinates = listOf(
                    are.points.toMutableList().also {
                        it.add(it.first())
                    }.map { it.geoJson }
                )
            )
        )
    }
}
