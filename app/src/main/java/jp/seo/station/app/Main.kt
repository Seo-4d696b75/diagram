package jp.seo.station.app

import jp.seo.diagram.core.KdTree
import jp.seo.diagram.core.KdTree.Node
import jp.seo.diagram.core.Rectangle
import jp.seo.diagram.core.VoronoiDiagram
import jp.seo.station.app.data.Result
import jp.seo.station.app.data.Station
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    require(args.size >= 2)
    calc(args[0], args[1])
}

private fun calc(srcFile: String, dstFile: String) {
    val json = Json { ignoreUnknownKeys = true }
    val src = File(srcFile).readText()
    val stations = json.decodeFromString<List<Station>>(src)
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
        if (s1 !is Station) throw ClassCastException()
        if (s2 !is Station) throw ClassCastException()
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
