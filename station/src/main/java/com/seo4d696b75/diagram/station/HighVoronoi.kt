package com.seo4d696b75.diagram.station

import com.seo4d696b75.diagram.core.HighVoronoi
import com.seo4d696b75.diagram.core.Rectangle
import com.seo4d696b75.diagram.station.model.GeoJsonFeature
import com.seo4d696b75.diagram.station.model.GeoJsonGeometry
import com.seo4d696b75.diagram.station.model.HighVoronoiStation
import com.seo4d696b75.diagram.station.model.StationPoint
import com.seo4d696b75.diagram.station.model.toGeoJSON
import kotlinx.serialization.json.JsonPrimitive

/**
 * 駅座標点の高次ボロノイ分割を計算する
 *
 * 駅全体の集合 `this` と隣接点情報 [HighVoronoiStation.next] が事前に全て把握できる場合の計算
 *
 * @param level 次数（１以上の整数）
 * @param center 中心の駅
 */
fun List<HighVoronoiStation>.calculateHighVoronoi(
    level: Int,
    center: HighVoronoiStation,
): GeoJsonFeature<GeoJsonGeometry.MultiPolygon> {
    val boundary = Rectangle(127.0, 46.0, 146.0, 26.0)
    val highVoronoi = HighVoronoi(boundary.container)
    val map = associateBy { it.code }
    val getStationByCode = { code: Int ->
        requireNotNull(map[code]) { "station(code:$code) not found" }
    }
    val provider = HighVoronoi.PointProvider { point ->
        require(point is StationPoint)
        val station = getStationByCode(point.code)
        station.next.map { nextCode -> getStationByCode(nextCode).toPoint() }
    }

    val result = highVoronoi.solve(level, center.toPoint(), provider)

    val geometry = GeoJsonGeometry.MultiPolygon(
        coordinates = result.map { polygon ->
            require(polygon.points.size >= 3)
            val points = buildList {
                addAll(polygon.points)
                add(polygon.points.first())
            }.map {
                it.toGeoJSON()
            }
            listOf(points)
        },
    )
    return GeoJsonFeature(
        geometry = geometry,
        properties = mapOf(
            "name" to JsonPrimitive(center.name),
            "code" to JsonPrimitive(center.code),
        ),
    )
}

private fun HighVoronoiStation.toPoint() = StationPoint(
    lat = lat,
    lng = lng,
    code = code,
    name = name,
)
