package jp.seo.station.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
        var next: List<Int>,
        var voronoi: VoronoiFeature,
    )
}
