package com.seo4d696b75.diagram.station.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 計算結果のモデル
 *
 * ### Kd-tree の表現
 * [root] に対応する [Station.code] を持つ駅座標がルートの頂点に該当し、
 * [Station.right], [Station.left] で子供の頂点の [Station.code] をそれぞれ指定する。
 */
@Serializable
data class Result(
    val root: Int,
    @SerialName("node_list")
    val nodes: List<Station>,
) {
    @Serializable
    data class Station(
        val lat: Double,
        val lng: Double,
        val code: Int,
        val name: String,
        var right: Int? = null,
        var left: Int? = null,

        /**
         * ボロノイ分割における隣接駅の [code] 集合
         *
         * この駅座標と各駅の座標を結ぶ線分がドロネー分割を構成する
         */
        var next: List<Int>,

        /**
         * この駅座標点のボロノイ分割領域
         */
        var voronoi: VoronoiFeature,
    )
}
