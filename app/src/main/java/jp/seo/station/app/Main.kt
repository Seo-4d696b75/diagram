package jp.seo.station.app

import jp.seo.diagram.core.KdTree.Node
import jp.seo.diagram.spherical.DelaunayDiagram
import jp.seo.diagram.spherical.LatLng
import jp.seo.diagram.spherical.Triangle
import jp.seo.station.app.data.Station
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    require(args.size >= 2)
    calc(args[0], args[1])
}

private fun calc(srcFile: String, dstFile: String) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    val src = File(srcFile).readText()
    val stations = json.decodeFromString<List<Station>>(src)
    println("station size: ${stations.size}")

    val points = stations.map { LatLng.euler(it.lat, it.lng) }
    val diagram = DelaunayDiagram(points)
    diagram.split(
        Triangle.points(
            LatLng.euler(0.0, 136.0),
            LatLng.euler(50.0, 100.0),
            LatLng.euler(50.0, 172.0),
        )
    )
    println("edge size: ${diagram.edges.size}")

}

private fun Node<Station>.traverseTree() {
    leftChild?.let { left ->
        point.left = left.point.code
        left.traverseTree()
    }
    rightChild?.let { right ->
        point.right = right.point.code
        right.traverseTree()
    }
}
