package jp.seo.diagram.spherical

import java.util.*

class DelaunayDiagram(points: Collection<LatLng>) {

    private val points = mutableSetOf<Vector>()
    private val solvedTriangles = mutableSetOf<Triangle>()
    private val solvedEdges = mutableSetOf<Edge>()
    private val solvedPairs = mutableMapOf<Edge, Pair<Triangle, Triangle?>>()

    private val triangles = mutableSetOf<Triangle>()
    private val trianglePairs = mutableMapOf<Edge, TrianglePair>()
    private val edgeQueue: Queue<Edge> = LinkedList()

    val edges: Set<Edge>
        get() = solvedEdges

    val edgeTriangleMap: Map<Edge, Pair<Triangle, Triangle?>>
        get() = solvedPairs

    init {
        this.points.addAll(points.map { it.vec })
    }

    fun split(border: Triangle) {
        val start = System.currentTimeMillis()

        triangles.clear()
        edgeQueue.clear()
        trianglePairs.clear()
        triangles.add(border)

        val ab = Edge.between(border.a, border.b)
        val bc = Edge.between(border.b, border.c)
        val ca = Edge.between(border.c, border.a)
        trianglePairs[ab] = TrianglePair(ab, border.c)
        trianglePairs[bc] = TrianglePair(bc, border.a)
        trianglePairs[ca] = TrianglePair(ca, border.b)

        points.forEachIndexed { i, p ->
            val t = getContainer(p)
            addPoint(p, t)
            print(String.format("\r%.2f%%", (i + 1) * 100.0 / points.size))
        }
        println()

        triangles.removeIf { isOnBorder(it, border) }

        solvedTriangles.clear()
        solvedTriangles.addAll(triangles)

        solvedEdges.also { set ->
            set.clear()
            solvedTriangles.forEach {
                set.add(Edge.between(it.a, it.b))
                set.add(Edge.between(it.b, it.c))
                set.add(Edge.between(it.c, it.a))
            }
        }

        solvedPairs.also { map ->
            map.clear()
            solvedEdges.forEach {
                val pair = trianglePairs[it] ?: throw NoSuchElementException()
                pair.removeBoundary(border)
                map[it] = pair.t1 to pair.t2
            }
        }

        println("completed ${System.currentTimeMillis() - start} ms")
    }

    private fun getContainer(p: Vector): Triangle {
        return triangles.find {
            it.contains(p)
        } ?: throw NoSuchElementException("Point $p outside border")
    }

    private fun addPoint(p: Vector, t: Triangle) {
        edgeQueue.clear()

        if (t.isVertex(p)) {
            return
        } else if (Edge.onEdge(t.a, t.b, p)) {
            addOnEdge(t.a, t.b, t.c, p)
        } else if (Edge.onEdge(t.b, t.c, p)) {
            addOnEdge(t.b, t.c, t.a, p)
        } else if (Edge.onEdge(t.c, t.a, p)) {
            addOnEdge(t.c, t.a, t.b, p)
        } else {
            addInTriangle(p, t)
        }

        resolveDelaunay()
    }

    private fun addInTriangle(p: Vector, t: Triangle) {
        require(triangles.remove(t))
        val ab = Edge.between(t.a, t.b)
        val bc = Edge.between(t.b, t.c)
        val ca = Edge.between(t.c, t.a)

        requireNotNull(trianglePairs[ab]).replace(p)
        requireNotNull(trianglePairs[bc]).replace(p)
        requireNotNull(trianglePairs[ca]).replace(p)

        val pa = Edge.between(p, t.a)
        val pb = Edge.between(p, t.b)
        val pc = Edge.between(p, t.c)
        val ta = TrianglePair(pa, t.b, t.c)
        val tb = TrianglePair(pb, t.c, t.a)
        val tc = TrianglePair(pc, t.a, t.b)
        trianglePairs[pa] = ta
        trianglePairs[pb] = tb
        trianglePairs[pc] = tc
        require(triangles.add(ta.t1))
        require(triangles.add(tb.t1))
        require(triangles.add(tc.t1))
        edgeQueue.offer(ab)
        edgeQueue.offer(bc)
        edgeQueue.offer(ca)
    }

    private fun addOnEdge(a: Vector, b: Vector, c: Vector, p: Vector) {
        val old = Edge.between(a, b)
        val pair = requireNotNull(trianglePairs[old])
        val p1 = pair.p1
        val p2 = requireNotNull(pair.p2)
        val d = if (old.line.onSameSide(p, p1)) p2 else p1

        triangles.remove(pair.t1)
        triangles.remove(pair.t2!!)
        val pc = Edge.between(p, c)
        val pd = Edge.between(p, d)
        val pa = Edge.between(p, a)
        val pb = Edge.between(p, b)
        val innerPair = TrianglePair(pc, a, b)
        val outerPair = TrianglePair(pd, a, b)
        triangles.add(innerPair.t1)
        triangles.add(innerPair.t2!!)
        triangles.add(outerPair.t1)
        triangles.add(outerPair.t2!!)
        trianglePairs[pc] = innerPair
        trianglePairs[pd] = outerPair
        trianglePairs[pa] = TrianglePair(pa, d, c)
        trianglePairs[pb] = TrianglePair(pb, d, c)

        val ac = Edge.between(a, c)
        val bc = Edge.between(b, c)
        val ad = Edge.between(a, d)
        val bd = Edge.between(b, d)
        requireNotNull(trianglePairs[ac]).replace(p)
        requireNotNull(trianglePairs[bc]).replace(p)
        requireNotNull(trianglePairs[ad]).replace(p)
        requireNotNull(trianglePairs[bd]).replace(p)
        edgeQueue.offer(ac)
        edgeQueue.offer(bc)
        edgeQueue.offer(ad)
        edgeQueue.offer(bd)
    }

    private fun resolveDelaunay() {
        while (edgeQueue.isNotEmpty()) {
            val edge = edgeQueue.poll()
            val pair = requireNotNull(trianglePairs[edge])
            if (pair.isFlip) {
                trianglePairs.remove(edge)
                require(triangles.remove(pair.t1))
                require(triangles.remove(requireNotNull(pair.t2)))
                pair.flip()

                trianglePairs[pair.edge] = pair
                require(triangles.add(pair.t1))
                require(triangles.add(requireNotNull(pair.t2)))

                val a1 = Edge.between(pair.p1, pair.edge.a)
                val b1 = Edge.between(pair.p1, pair.edge.b)
                val a2 = Edge.between(requireNotNull(pair.p2), pair.edge.a)
                val b2 = Edge.between(requireNotNull(pair.p2), pair.edge.b)
                edgeQueue.offer(a1)
                edgeQueue.offer(b1)
                edgeQueue.offer(a2)
                edgeQueue.offer(b2)
                requireNotNull(trianglePairs[a1]).replace(pair.edge.b)
                requireNotNull(trianglePairs[b1]).replace(pair.edge.a)
                requireNotNull(trianglePairs[a2]).replace(pair.edge.b)
                requireNotNull(trianglePairs[b2]).replace(pair.edge.a)
            }
        }
    }

    private fun isOnBorder(t: Triangle, border: Triangle): Boolean {
        return t.isVertex(border.a)
                || t.isVertex(border.b)
                || t.isVertex(border.c)
    }
}

private class TrianglePair {
    var t1: Triangle
    var t2: Triangle?
    var edge: Edge
    var p1: Vector
    var p2: Vector?

    constructor(edge: Edge, p1: Vector, p2: Vector) {
        t1 = Triangle.points(edge.a, edge.b, p1)
        t2 = Triangle.points(edge.a, edge.b, p2)
        this.edge = edge
        this.p1 = p1
        this.p2 = p2
    }

    constructor(edge: Edge, p: Vector) {
        t1 = Triangle.points(edge.a, edge.b, p)
        t2 = null
        this.edge = edge
        p1 = p
        p2 = null
    }

    fun replace(p: Vector) {
        if ((p1 - p).isZero) {
            throw RuntimeException()
        }
        if (edge.line.onSameSide(p1, p)) {
            t1 = Triangle.points(edge.a, edge.b, p)
            p1 = p
        } else if (p2 != null) {
            t2 = Triangle.points(edge.a, edge.b, p)
            p2 = p
        } else {
            throw RuntimeException()
        }
    }

    fun removeBoundary(container: Triangle) {
        if (container.isVertex(p1)) {
            p1 = requireNotNull(p2)
            t1 = requireNotNull(t2)
            p2 = null
            t2 = null
        } else if (p2?.let(container::isVertex) == true) {
            p2 = null
            t2 = null
        }
    }

    val isFlip: Boolean
        get() = p2?.let { t1.circumcircle.contains(it) } ?: false

    fun flip() {
        val old = edge
        edge = Edge.between(p1, requireNotNull(p2))
        p1 = old.a
        p2 = old.b
        t1 = Triangle.points(edge.a, edge.b, old.a)
        t2 = Triangle.points(edge.a, edge.b, old.b)
    }
}