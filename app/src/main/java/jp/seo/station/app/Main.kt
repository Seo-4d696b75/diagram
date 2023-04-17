package jp.seo.station.app

import jp.seo.diagram.core.KdTree
import jp.seo.diagram.core.KdTree.Node
import jp.seo.diagram.spherical.*
import jp.seo.station.app.data.Result
import jp.seo.station.app.data.Station
import jp.seo.station.app.data.Voronoi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
    val diagram = VoronoiDiagram(points)
    diagram.split(
        Triangle.points(
            LatLng.euler(0.0, 136.0),
            LatLng.euler(50.0, 100.0),
            LatLng.euler(50.0, 172.0),
        )
    )
    println("edge size: ${diagram.edges.size}")

    val map = mutableMapOf<Vector, Station>().apply {
        stations.forEach {
            val v = LatLng.euler(it.lat, it.lng).vec
            put(v, it)
        }
    }

    stations.forEach {
        val p = LatLng.euler(it.lat, it.lng).vec
        it.voronoi = requireNotNull(diagram.area[p]).voronoi
        it.next = mutableListOf()
    }

    diagram.delaunayEdges.forEach {
        val s1 = requireNotNull(map[it.a])
        val s2 = requireNotNull(map[it.b])
        s1.next?.add(s2.code)
        s2.next?.add(s1.code)
    }

    println("build Kd-tree")
    val tree = KdTree(stations)
    tree.root.traverseTree()

    val result = Result(
        root = tree.root.point.code,
        nodes = stations,
    )
    val dst = json.encodeToString(result)
    File(dstFile).writeText(dst)
}

val VoronoiArea.voronoi: Voronoi
    get() = Voronoi(
        points.map { jp.seo.station.app.data.LatLng(it.lat.euler, it.lng.euler) },
        enclosed,
    )

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
