package jp.seo.diagram.spherical

import java.util.*
import kotlin.math.acos

class Edge private constructor(
    val a: Vector,
    val b: Vector,
) {

    companion object {
        /**
         * ２点A,Bを結ぶ直線
         *
         * 直線の長さが測地的距離・大圏距離になる
         */
        fun between(a: Vector, b: Vector): Edge {
            if ((a - b).isZero) throw IllegalArgumentException("line can't be determined by duplicated points")
            if ((a + b).isZero) throw IllegalArgumentException("pair of antipodes can't determine a line")
            return if (a < b) Edge(a, b) else Edge(b, a)
        }

        fun between(a: LatLng, b: LatLng) = between(a.vec, b.vec)

        fun onEdge(a: Vector, b: Vector, p: Vector): Boolean {
            return Line.onLine(a, b, p)
                    && (acos(p.dot(a)) + acos(p.dot(b)) - acos(a.dot(b))).isZero
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is Edge
                && a == other.a
                && b == other.b
    }

    override fun hashCode() = Objects.hash(a, b)

    override fun toString() = "Edge($a - $b)"

    val length: Double
        get() = acos(a.dot(b))

    val line: Line
        get() = Line.between(a, b)

    private fun onEdge(p: Vector): Boolean {
        val al = acos(a.dot(p))
        val bl = acos(b.dot(p))
        return (al + bl - length).isZero
    }

    fun intersection(other: Line): Vector? {
        val p1 = line.intersection(other)
        if (onEdge(p1)) return p1
        val p2 = -p1
        if (onEdge(p2)) return p2
        return null
    }

    fun intersection(other: Edge): Vector? {
        val p = other.intersection(line) ?: return null
        return if (onEdge(p)) p else null
    }
}