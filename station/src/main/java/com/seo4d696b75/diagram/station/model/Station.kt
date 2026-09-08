package com.seo4d696b75.diagram.station.model

import com.seo4d696b75.diagram.core.Point
import com.seo4d696b75.diagram.core.VoronoiDiagram.VoronoiArea
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 入力の駅座標点モデル
 *
 * 座標点のリストがJSON形式で入力される。
 * 座標 [lat], [lng] はいずれも10進小数のオイラー角。
 */
@Serializable
data class Station(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
)

/**
 * data model while calculating (mutable)
 */
internal class StationPoint(
    val lat: Double,
    val lng: Double,
    val code: Int,
    val name: String,
    var right: Int? = null,
    var left: Int? = null,
    var next: MutableList<Int>? = null,
    var voronoi: VoronoiArea? = null,
) : Point() {
    constructor(raw: Station) : this(
        lat = raw.lat,
        lng = raw.lng,
        code = raw.code,
        name = raw.name,
    )

    override fun getX() = lng

    override fun getY() = lat

    override fun toString(): String = "$name($code)"

    fun toResult() =
        Result.Station(
            lat = lat,
            lng = lng,
            code = code,
            name = name,
            right = right,
            left = left,
            next = requireNotNull(next),
            voronoi =
                requireNotNull(voronoi).let { area ->
                    val geometry =
                        if (area.enclosed) {
                            VoronoiGeometry.Polygon(
                                coordinates =
                                    listOf(
                                        area.points
                                            .toMutableList()
                                            .also { list ->
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
