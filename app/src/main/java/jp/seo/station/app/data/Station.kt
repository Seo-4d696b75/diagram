package jp.seo.station.app.data

import jp.seo.diagram.core.Point
import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * json input model
 */
@Serializable
data class RawStation(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
)

/**
 * data model while calculating (mutable)
 */
class Station(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
    var right: Int? = null,
    var left: Int? = null,
    var next: MutableList<Int>? = null,
    var voronoi: VoronoiArea? = null,
) : Point() {

    constructor(raw: RawStation) : this(
        lat = raw.lat,
        lng = raw.lng,
        code = raw.code,
        name = raw.name,
    )

    override fun getX() = lng

    override fun getY() = lat

    override fun toString(): String {
        return "$name($code)"
    }

    fun toResult() = Result.Station(
        lat = lat,
        lng = lng,
        code = code,
        name = name,
        right = right,
        left = left,
        next = requireNotNull(next),
        voronoi = requireNotNull(voronoi).let { area ->
            val geometry = if (area.enclosed) {
                VoronoiGeometry.Polygon(
                    coordinates = listOf(
                        area.points.toMutableList().also { list ->
                            list.add(list.first())
                        }.map { it.toGeoJSON() },
                    ),
                )
            } else {
                VoronoiGeometry.LineString(
                    coordinates = area.points.map { it.toGeoJSON() },
                )
            }
            VoronoiFeature(geometry = geometry)
        },
    )
}

private fun Double.toFixed(digit: Int = 6): Double {
    assert(digit > 0)
    val scale = 10.0.pow(digit)
    return (this * scale).roundToInt() / scale
}

private fun Point.toGeoJSON(): List<Double> = listOf(x.toFixed(), y.toFixed())