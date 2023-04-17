package jp.seo.station.app.data

import jp.seo.diagram.core.Point
import jp.seo.station.app.data.geo.VoronoiSerializer
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

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
    var voronoi: Voronoi? = null,
) : Point() {
    override fun getX() = lng

    override fun getY() = lat

    override fun toString(): String {
        return "$name($code)"
    }
}

data class Voronoi(
    val points: List<LatLng>,
    val enclosed: Boolean,
)

data class LatLng(
    val lat: Double,
    val lng: Double,
) {
    val geoJson: List<Double>
        get() = listOf(lng.toFixed(), lat.toFixed())
}

fun Double.toFixed(digit: Int = 6): Double {
    assert(digit > 0)
    val scale = 10.0.pow(digit)
    return (this * scale).roundToInt() / scale
}
