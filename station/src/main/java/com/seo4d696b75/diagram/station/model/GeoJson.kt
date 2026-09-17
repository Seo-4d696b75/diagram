package com.seo4d696b75.diagram.station.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * [GeoJSON](https://geojson.org/) の Feature オブジェクト
 */
@Serializable
data class GeoJsonFeature(
    val type: String = "Feature",
    val geometry: GeoJsonGeometry,
    val properties: Map<String, JsonElement> = emptyMap(),
)

/**
 * [GeoJSON](https://geojson.org/) の Geometry オブジェクト（実際の図形データ）
 */
@Serializable
sealed interface GeoJsonGeometry {
    val coordinates: List<*>

    /**
     * ポリゴンの図形データ
     *
     * `coordinates[0]` が座標点のリストに該当します。最初と最後の点は必ず一致します。
     * ```json
     *      {
     *          "type": "Polygon",
     *          "coordinates": [
     *              [
     *                  [100.0, 0.0],
     *                  [101.0, 0.0],
     *                  [101.0, 1.0],
     *                  [100.0, 1.0],
     *                  [100.0, 0.0]
     *              ]
     *          ]
     *      }
     * ```
     *
     * 本ライブラリでは穴を扱わないため [coordinates] のサイズは常に`1`です
     *
     * [RFC サンプル](https://datatracker.ietf.org/doc/html/rfc7946#appendix-A.3)
     */
    @Serializable
    @SerialName("Polygon")
    data class Polygon(
        /**
         * ポリゴンの座標点
         *
         * 穴は扱わないためサイズは常に`1`です
         *
         * - `coordinates[0][i][0]`: 経度
         * - `coordinates[0][i][1]`: 緯度
         */
        override val coordinates: List<List<List<Double>>>,
    ) : GeoJsonGeometry

    /**
     * 点と点を順に繋いだポリライン
     *
     * [RFC サンプル](https://datatracker.ietf.org/doc/html/rfc7946#appendix-A.2)
     */
    @Serializable
    @SerialName("LineString")
    data class LineString(
        /**
         * 座標点
         *
         * - `coordinates[i][0]`: 経度
         * - `coordinates[i][1]`: 緯度
         */
        override val coordinates: List<List<Double>>,
    ) : GeoJsonGeometry
}
