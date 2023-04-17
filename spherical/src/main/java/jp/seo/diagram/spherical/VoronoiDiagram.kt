package jp.seo.diagram.spherical

class VoronoiDiagram constructor(
    points: Collection<LatLng>
) : DelaunayDiagram(points) {

    private val _edge = mutableSetOf<Edge>()
    private val _area = mutableMapOf<Vector, VoronoiArea>()

    override val edges: Set<Edge>
        get() = _edge

    val delaunayEdges: Set<Edge>
        get() = super.edges

    val area: Map<Vector, VoronoiArea>
        get() = _area

    override fun split(border: Triangle) {
        super.split(border)
        _area.clear()
        _edge.clear()

        val addEdge = { e: Edge, p: Vector ->
            _area.computeIfAbsent(p) { VoronoiArea() }
            requireNotNull(_area[p]).addEdge(e)
        }

        println("calc voronoi")

        super.edges.forEach { edge ->
            val pair = requireNotNull(edgeTriangleMap[edge])
            val t1 = pair.first
            val t2 = pair.second
            if (t2 != null) {
                val p1 = t1.circumcircle.center
                val p2 = t2.circumcircle.center
                val b = Edge.between(p1, p2)
                // TODO borderの外側にはみ出る場合はどうする？
                _edge.add(b)
                addEdge(b, edge.a)
                addEdge(b, edge.b)
            } else {
                // TODO borderの外側
                val p = t1.circumcircle.center
                val b = Edge.between(p, (edge.a + edge.b) / 2.0)
                _edge.add(b)
                addEdge(b, edge.a)
                addEdge(b, edge.b)
            }
        }
    }
}

class VoronoiArea {

    internal fun addEdge(edge: Edge) {
        if (list.isEmpty()) {
            list.add(edge.a)
            list.add(edge.b)
            start = edge.a
            end = edge.b
        } else if (merge(edge)) {
            while (true) {
                if (!pool.removeIf(this::merge) || pool.isEmpty()) break
            }
        } else {
            pool.add(edge)
        }
    }

    private fun merge(edge: Edge): Boolean {
        return when {
            edge.a == start -> {
                list.add(0, edge.b)
                start = edge.b
                true
            }

            edge.a == end -> {
                list.add(edge.b)
                end = edge.b
                true
            }

            edge.b == start -> {
                list.add(0, edge.a)
                start = edge.a
                true
            }

            edge.b == end -> {
                list.add(edge.a)
                end = edge.a
                true
            }

            else -> false
        }
    }

    private val pool = mutableListOf<Edge>()
    private val list = mutableListOf<Vector>()
    private var start: Vector? = null
    private var end: Vector? = null

    val enclosed: Boolean
        get() {
            require(pool.isEmpty())
            return start == end
        }

    val points: List<LatLng>
        get() {
            require(pool.isEmpty())
            return mutableListOf(*list.toTypedArray()).apply {
                if (start == end) removeAt(0)
            }.map { LatLng.vec(it) }
        }
}
