package jp.seo.station.app.data

import jp.seo.diagram.core.Point
import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import jp.seo.station.app.data.geo.VoronoiSerializer
import kotlinx.serialization.Serializable

@Serializable
class Station(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
    var right: Int? = null,
    var left: Int? = null,
    var next: MutableList<Int>? = null,
    @Serializable(with = VoronoiSerializer::class)
    var voronoi: VoronoiArea? = null,
): Point() {
    override fun getX() = lng

    override fun getY() = lat

    override fun toString(): String {
        return "$name($code)"
    }
}
