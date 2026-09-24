package com.seo4d696b75.diagram.core

import java.util.LinkedList
import java.util.Queue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * ドロネー図における母点を結ぶ線分の垂直二等分線は互いに交差し、その交点によって二等分線は分割される.
 * その交点をNode、交点によって分割されてできた線分を辺Edgeにもつグラフ構造を考える
 * 
 * @author Seo-4d696b75
 * @version 2019/06/12
 */
class HighVoronoi(
    /**
     * 分割する母点をすべて内部に含む三角形
     */
    private val container: Triangle,
) {
    /**
     * 高次ボロノイ図を解決するうえで必要となる頂点を提供する.
     *
     * ボロノイ図の双対図であるドロネー図が解決済みであると仮定して以下のように頂点の隣接関係を定義する.
     *
     * **隣接する点 = ドロネー図の各辺における両端点**
     */
    fun interface PointProvider {
        /**
         * 指定された点に隣接する頂点集合を返す
         *
         * @param point 現在の点
         * @return 隣接点の集合。条件を満たす要素がない場合は空のリスト。
         */
        fun getNeighbors(point: Point): Iterable<Point>
    }

    interface ResultCallback {
        /**
         * 各n次ボロノイ図が計算されると順次呼ばれる
         * 
         * @param index  0始まりでカウントした次数 `[0,level)`
         * @param points 閉じた多角形
         */
        fun onResolved(index: Int, points: Polygon, milliseconds: Long)

        fun onCompleted(results: List<Polygon>, milliseconds: Long)
    }

    private lateinit var center: Point
    private lateinit var bisectors: MutableList<Bisector>
    private lateinit var provider: PointProvider
    private lateinit var requestedPoint: MutableSet<Point>
    private lateinit var addedPoint: MutableSet<Point>
    private lateinit var requestQueue: Queue<Point>

    /**
     * 計算する
     * 
     * @param level    [1,level]の次数に関して計算する
     * @param center   目的の中心点
     * @param provider 隣接頂点を定義するオブジェクト
     * @param callback 各次数で計算が終わる度にコールされる
     * @return [1,level]の次数で計算された多角形の配列, [index-1]のポリゴンがindex次の解
     */
    fun solve(
        level: Int,
        center: Point,
        provider: PointProvider,
        callback: ResultCallback? = null,
    ): List<Polygon> {
        require(level >= 1) { "level must be >= 1" }
        this.center = center
        this.provider = provider

        Setting.error = 2.0.pow(-30.0)

        val time = System.currentTimeMillis()

        val result = mutableListOf<Polygon>()
        bisectors = mutableListOf()

        addBoundary(Line(container.a, container.b))
        addBoundary(Line(container.b, container.c))
        addBoundary(Line(container.c, container.a))

        addedPoint = mutableSetOf(center)
        requestedPoint = mutableSetOf(center)
        requestQueue = LinkedList()


        for (point in provider.getNeighbors(center)) {
            addedPoint.add(point)
            addBisector(point)
        }

        var list: List<Node>? = null

        for (targetLevel in 1..level) {
            val loopTime = System.currentTimeMillis()

            list = traverse(list)
            for (n in list) n.onSolved(targetLevel)

            val polygon = Polygon(list)
            result.add(polygon)

            expandDelaunayPoints()

            callback?.onResolved(targetLevel - 1, polygon, System.currentTimeMillis() - loopTime)
        }


        for (bisector in bisectors) {
            bisector.release()
        }

        callback?.onCompleted(result, System.currentTimeMillis() - time)
        return result
    }

    private fun traverse(previousNodes: List<Node>?): List<Node> {
        var (
            next: Node,
            previous: Point,
        ) = previousNodes.let { list ->
            var next: Node? = null
            var previous: Point
            if (list == null) {
                val history = mutableSetOf<Point>()
                val sample = bisectors[0]
                next = requireNotNull(sample.intersections[1].node) {
                    "first node of an intersection not found (level=1)"
                }
                previous = sample.intersections[0]
                while (history.add(next!!)) {
                    val current = next
                    next = current.nextDown(previous)
                    previous = current
                }
            } else {
                previous = list.last()
                for (n in list) {
                    next = n.nextUp(previous)
                    previous = n
                    if (next != null && !next.hasSolved()) break
                }
            }

            requireNotNull(next) {
                "traverse start node not found."
            }
            require(!next.hasSolved()) {
                "traverse start node must NOT been solved."
            }

            next to previous
        }

        val start = next

        return buildList {
            add(start)
            while (true) {
                requestExtension(next.p1.line.delaunayPoint)
                requestExtension(next.p2.line.delaunayPoint)
                val current = next
                next = current.next(previous)
                previous = current
                if (start == next) break
                add(next)
            }
        }
    }

    private fun requestExtension(point: Point?) {
        if (point != null && requestedPoint.add(point)) {
            for (p in provider.getNeighbors(point)) {
                if (addedPoint.add(p)) {
                    requestQueue.offer(p)
                }
            }
        }
    }

    private fun expandDelaunayPoints() {
        while (requestQueue.isNotEmpty()) {
            val request = requestQueue.remove()
            addBisector(request)
        }
    }

    private fun addBoundary(self: Line) {
        val boundary = Bisector(self)
        for (preexist in bisectors) {
            val p = boundary.line.getIntersection(preexist.line)
            val a = Intersection(p, boundary)
            val b = Intersection(p, preexist)
            val n = Node(p, a, b)
            a.node = n
            b.node = n
            boundary.addIntersection(a)
            preexist.addIntersection(b)
        }
        bisectors.add(boundary)
    }

    private fun addBisector(point: Point) {
        val bisector = Bisector(point, Line.getPerpendicularBisector(point, center))
        for (preexist in bisectors) {
            val p = bisector.line.getIntersection(preexist.line)
            if (p != null && container.containsPoint(p)) {
                val a = Intersection(center, p, bisector, preexist.line)
                val b = Intersection(center, p, preexist, bisector.line)
                val n = Node(p, a, b)
                a.node = n
                b.node = n

                bisector.addIntersection(a)
                preexist.addIntersection(b)
            }
        }
        bisectors.add(bisector)
    }

    private class Node(
        val point: Point,
        a: Intersection,
        b: Intersection,
    ) : Point() {
        val onBoundary: Boolean
        private var _p1: Intersection? = null
        private var _p2: Intersection? = null
        private var index = 0f

        val p1: Intersection
            get() = requireNotNull(_p1) { "already released" }
        val p2: Intersection
            get() = requireNotNull(_p2) { "already released" }

        init {
            _p1 = a
            _p2 = b
            var cnt = 0
            if (a.line.isBoundary) cnt++
            if (b.line.isBoundary) cnt++
            when (cnt) {
                0 -> {
                    onBoundary = false
                    index = -1f
                }

                1 -> {
                    onBoundary = true
                    index = -1f
                }

                else -> {
                    onBoundary = false
                }
            }
        }

        override fun getX(): Double {
            return point.getX()
        }

        override fun getY(): Double {
            return point.getY()
        }

        /**
         * 辿ってきた辺とは異なる線分上の隣接頂点でかつ辺のボロノイ次数が同じになる方を返す.
         * 
         * @param previous from which you are traversing
         * @return Voronoi-Index of Edge:previous=>this is same as that of Edge:this=>next
         */
        fun next(previous: Point): Node {
            val p1 = this.p1
            val p2 = this.p2
            return if (p1.hasNext() && p1.next() == previous) {
                next(p1, p2, false, -p1.step)
            } else if (p1.hasPrevious() && p1.previous() == previous) {
                next(p1, p2, true, p1.step)
            } else if (p2.hasNext() && p2.next() == previous) {
                next(p2, p1, false, -p2.step)
            } else if (p2.hasPrevious() && p2.previous() == previous) {
                next(p2, p1, true, p2.step)
            } else {
                throw NoSuchElementException("next not found")
            }
        }

        fun next(current: Intersection, other: Intersection, forward: Boolean, step: Int): Node {
            val intersection = if (onBoundary && index > 0) {
                // 頂点がFrame境界線上（Vertexではない）でかつ
                // この頂点が解決済みなら無視して同じ境界線上のお隣さんへ辿る
                if (forward) current.next() else current.previous()
            } else {
                // 頂点がFrame内部なら step = Node.STEP_UP/DOWN　のいずれか
                // FrameのVertexに位置する場合は例外的に step = Node.STEP_ZERO
                other.neighbor(-step)
            }
            return requireNotNull(intersection.node) {
                "node reference not found. $intersection"
            }
        }

        /**
         * 辿ってきた辺とは異なる線分上の隣接頂点のうちこの頂点から見てボロノイ次数が
         * 下がるまたは変化しない方を返す.
         *
         * この頂点がFrame内部なら必ず次数が下がる隣接頂点を返すが、
         * Frame境界線のVertexに相当する場合は例外的に次数変化0の方向の頂点を返す
         * 
         * @param previous from which you are traversing
         */
        fun nextDown(previous: Point?): Node {
            val target = when {
                p1.isNeighbor(previous) -> p2
                p2.isNeighbor(previous) -> p1
                else -> throw NoSuchElementException("not found")
            }
            val intersection = if (target.hasNeighbor(STEP_DOWN)) {
                target.neighbor(STEP_DOWN)
            } else {
                target.neighbor(STEP_ZERO)
            }
            return requireNotNull(intersection.node) {
                "node reference not found. $intersection"
            }
        }

        /**
         * この頂点から見てボロノイ次数が上がる方向の隣接頂点を返す
         *
         * 辿ってきた方向`previous => this`に対して異なる線分上、同じ線分上の順で探す。
         * それでも存在しない場合はNull
         * 
         * @param previous from which you are traversing
         * @return Null if no such node
         */
        fun nextUp(previous: Point?): Node? {
            val (t1, t2) = when {
                p1.isNeighbor(previous) -> p2 to p1
                p2.isNeighbor(previous) -> p1 to p2
                else -> throw NoSuchElementException("not found")
            }
            val intersection = when {
                t1.hasNeighbor(STEP_UP) -> t1.neighbor(STEP_UP)
                t2.hasNeighbor(STEP_UP) -> t2.neighbor(STEP_UP)
                else -> return null
            }
            return requireNotNull(intersection.node) {
                "node reference not found. $intersection"
            }
        }

        fun onSolved(level: Int) {
            p1.onSolved()
            p2.onSolved()
            if (index < 0) {
                index = if (p1.line.isBoundary || p2.line.isBoundary) {
                    // フレーム上の頂点はn次以上のボロノイ図に登場する
                    level.toFloat()
                } else {
                    // フレーム内部の頂点はn,n+1次のボロノイ図にしか登場しない
                    level + 0.5f
                }
            } else if (index.roundToInt().toFloat() != index) {
                // 整合性の確認
                require(index + 0.5f == level.toFloat()) {
                    "index mismatch"
                }
            }
        }

        fun hasSolved(): Boolean {
            return index >= 0f
        }

        fun release() {
            _p1 = null
            _p2 = null
        }

        override fun toString(): String {
            if (index < 0) {
                return String.format("%s not solved", super.toString())
            } else if (index == 0f) {
                return String.format("%s Vertex", super.toString())
            } else {
                return String.format("%s %.1f", super.toString(), index)
            }
        }

        companion object {
            const val STEP_UP: Int = 1
            const val STEP_DOWN: Int = -1
            const val STEP_ZERO: Int = 0
        }
    }

    private class Intersection : Point {
        constructor(center: Point, point: Point, line: Bisector, other: Line) {
            this.point = point
            this.line = line

            var dx = line.line.b
            var dy = -line.line.a
            if (dx < 0 || (dx == 0.0 && dy < 0)) {
                dx *= -1.0
                dy *= -1.0
            }
            val p: Point = BasePoint(point.getX() + dx, point.getY() + dy)
            this.step = if (other.onSameSide(
                    p,
                    center
                )
            ) Node.STEP_DOWN else Node.STEP_UP
        }

        constructor(point: Point, line: Bisector) {
            this.point = point
            this.line = line
            this.step = Node.STEP_ZERO
        }

        val point: Point
        val line: Bisector
        val step: Int
        var node: Node? = null

        private var index = -1
        private var previous: Intersection? = null
        private var next: Intersection? = null

        override fun getX(): Double {
            return point.getX()
        }

        override fun getY(): Double {
            return point.getY()
        }


        fun insert(previous: Intersection?, next: Intersection?, index: Int) {
            this.previous = previous
            this.next = next
            if (previous != null) previous.next = this
            if (next != null) {
                next.previous = this
                next.incrementIndex()
            }
            this.index = index
        }

        fun incrementIndex() {
            index++
            if (next != null) next!!.incrementIndex()
        }

        fun hasPrevious(): Boolean {
            return previous != null
        }

        fun hasNext(): Boolean {
            return next != null
        }

        fun isNeighbor(p: Point?): Boolean {
            return (hasNext() && next == p) || (hasPrevious() && previous == p)
        }

        fun hasNeighbor(step: Int): Boolean {
            if (step == 0 && this.step == 0) {
                return true
            } else if (step != 0 && this.step != 0) {
                return if (step == this.step) hasNext() else hasPrevious()
            }
            return false
        }

        fun neighbor(step: Int): Intersection {
            if (step == 0 && this.step == 0) {
                return requireNotNull(previous ?: next) {
                    "previous nor next is null."
                }
            } else if (step != 0 && this.step != 0) {
                return if (step == this.step) next() else previous()
            }
            throw IllegalArgumentException("invalid step: $step")
        }

        fun next(): Intersection = requireNotNull(next)

        fun previous(): Intersection = requireNotNull(previous)

        fun getIndex(): Int {
            require(index >= 0) { "index not set yet" }
            return index
        }

        fun onSolved() {
            line.onIntersectionSolved(this)
        }

        fun release() {
            previous = null
            next = null
            if (node == null) {
                return
            }
            node?.release()
            node = null
        }

        override fun toString(): String {
            return String.format("%s %+d", super.toString(), step)
        }
    }

    private class Bisector {
        constructor(point: Point?, line: Line) {
            this.delaunayPoint = point
            this.line = line
            intersections = LinkedList<Intersection>()
            isBoundary = false
        }

        // special for boundary line
        constructor(edge: Line) {
            delaunayPoint = null
            line = edge
            isBoundary = true
            intersections = LinkedList<Intersection>()
        }

        val intersections: MutableList<Intersection>
        private var solvedPointIndexFrom = Int.MAX_VALUE
        private var solvedPointIndexTo = -1

        val delaunayPoint: Point?
        val line: Line
        val isBoundary: Boolean

        fun onIntersectionSolved(intersection: Intersection) {
            val index = intersection.getIndex()
            solvedPointIndexFrom = min(solvedPointIndexFrom, index)
            solvedPointIndexTo = max(solvedPointIndexTo, index)
        }

        fun addIntersection(intersection: Intersection) {
            val size = intersections.size
            val index = addIntersection(intersection, 0, size)

            intersection.insert(
                if (index > 0) intersections[index - 1] else null,
                if (index < size) intersections[index] else null,
                index
            )
            intersections.add(index, intersection)
            if (solvedPointIndexFrom < solvedPointIndexTo) {
                if (index <= solvedPointIndexFrom) {
                    solvedPointIndexFrom++
                    solvedPointIndexTo++
                } else {
                    require(index > solvedPointIndexTo) {
                        "new intersection added to resolved-range!!"
                    }
                }
            }
        }

        fun addIntersection(point: Intersection, indexFrom: Int, indexTo: Int): Int {
            if (indexFrom == indexTo) {
                return indexFrom
            } else {
                val midIndex = (indexFrom + indexTo - 1) / 2
                val mid = intersections[midIndex]
                return when {
                    point < mid -> addIntersection(point, indexFrom, midIndex)
                    point > mid -> addIntersection(point, midIndex + 1, indexTo)
                    else -> throw IllegalArgumentException()
                }
            }
        }

        fun release() {
            for (item in intersections) item.release()
            intersections.clear()
        }
    }
}

