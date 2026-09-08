package com.seo4d696b75.diagram.station

import com.seo4d696b75.diagram.core.KdTree
import com.seo4d696b75.diagram.core.KdTree.Node
import com.seo4d696b75.diagram.core.Rectangle
import com.seo4d696b75.diagram.core.VoronoiDiagram
import com.seo4d696b75.diagram.station.model.Result
import com.seo4d696b75.diagram.station.model.Station
import com.seo4d696b75.diagram.station.model.StationPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 駅座標点集合のドロネー・ボロノイ分割と Kd-tree 構造を計算する
 *
 * @param srcFile [Station] のリストに相当するJSONファイルのパス
 * @param dstFile [Result] のJSONファイルを書き出すパス
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
fun calculateStationDiagram(
    srcFile: String,
    dstFile: String,
) {
    val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }
    val src = File(srcFile).readText()
    val input = json.decodeFromString(ListSerializer(Station.serializer()), src)
    val result = input.calculateDiagram()
    val dst = json.encodeToString(Result.serializer(), result)
    File(dstFile).writeText(dst)
}

/**
 * 駅座標点集合のドロネー・ボロノイ分割と Kd-tree 構造を計算する
 */
fun List<Station>.calculateDiagram(): Result {
    val stations = map(::StationPoint)
    println("station size: ${stations.size}")
    val diagram = VoronoiDiagram(stations)
    diagram.split(Rectangle(112.0, 60.0, 160.0, 20.0))
    println("edge size: ${diagram.edges.size}")

    stations.forEach {
        it.voronoi = diagram.getVoronoiArea(it)
        it.next = mutableListOf()
    }
    diagram.delaunayEdges.forEach {
        val s1 = it.a
        val s2 = it.b
        if (s1 !is StationPoint) throw ClassCastException()
        if (s2 !is StationPoint) throw ClassCastException()
        s1.next?.add(s2.code)
        s2.next?.add(s1.code)
    }

    println("build Kd-tree")
    val tree = KdTree(stations)
    tree.root.traverseTree()

    return Result(
        root = tree.root.point.code,
        nodes = stations.map(StationPoint::toResult),
    )
}

private fun Node<StationPoint>.traverseTree() {
    leftChild?.let { left ->
        point.left = left.point.code
        left.traverseTree()
    }
    rightChild?.let { right ->
        point.right = right.point.code
        right.traverseTree()
    }
}
