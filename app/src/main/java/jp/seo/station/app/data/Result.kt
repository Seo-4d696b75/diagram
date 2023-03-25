package jp.seo.station.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val root: Int,
    @SerialName("node_list")
    val nodes: List<Station>
)
