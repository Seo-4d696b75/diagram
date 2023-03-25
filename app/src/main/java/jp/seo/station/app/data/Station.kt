package jp.seo.station.app.data

import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import jp.seo.station.app.data.geo.VoronoiSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
    var right: Int? = null,
    var left: Int? = null,
    var next: List<Int>? = null,
    @Serializable(with = VoronoiSerializer::class)
    var voronoi: VoronoiArea? = null,
)
