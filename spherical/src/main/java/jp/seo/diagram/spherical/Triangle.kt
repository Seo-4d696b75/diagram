package jp.seo.diagram.spherical

import java.util.*
import kotlin.math.acos

class Triangle private constructor(
    val a: Vector,
    val b: Vector,
    val c: Vector,
) {

    companion object {
        fun points(a: Vector, b: Vector, c: Vector): Triangle {
            val points = mutableListOf(a, b, c).sorted()
            val p1 = points[0]
            val p2 = points[1]
            val p3 = points[2]
            if (p1.minus(p2).isZero || p2.minus(p3).isZero) {
                throw IllegalArgumentException("duplicated points found")
            }
            if (p1.plus(p2).isZero || p2.plus(p3).isZero || p3.plus(p1).isZero) {
                throw IllegalArgumentException("antipodes found")
            }
            return Triangle(p1, p2, p3)
        }

        fun points(a: LatLng, b: LatLng, c: LatLng) = points(a.vec, b.vec, c.vec)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is Triangle
                && a == other.a
                && b == other.b
                && c == other.c
    }

    override fun hashCode() = Objects.hash(a, b, c)

    override fun toString() = "Triangle($a - $b - $c)"

    fun intersections(line: Line): List<Vector> {
        val ab = Edge.between(a, b)
        val bc = Edge.between(b, c)
        val ca = Edge.between(c, a)
        val result = mutableListOf<Vector>()
        ab.intersection(line)?.let { result.add(it) }
        bc.intersection(line)?.let { result.add(it) }
        ca.intersection(line)?.let { result.add(it) }
        return result.sorted()
    }

    fun diagonal(p: Vector): Edge {
        return if (p.minus(a).isZero) {
            Edge.between(b, c)
        } else if (p.minus(b).isZero) {
            Edge.between(c, a)
        } else if (p.minus(c).isZero) {
            Edge.between(a, b)
        } else throw IllegalArgumentException("point not found")
    }

    fun contains(p: Vector): Boolean {
        val ab = a.cross(b).normalize
        val bc = b.cross(c).normalize
        val ca = c.cross(a).normalize
        return ab.dot(p) * ab.dot(c) >= zero
                && bc.dot(p) * bc.dot(a) >= zero
                && ca.dot(p) * ca.dot(b) >= zero
    }

    fun isVertex(p: Vector): Boolean {
        return p.minus(a).isZero
                || p.minus(b).isZero
                || p.minus(c).isZero
    }

    private var _circumcircle: Circle? = null

    val circumcircle: Circle
        get() {
            val c = _circumcircle ?: run {
                val m = Matrix.from(
                    arrayOf(
                        a.x, a.y, a.z,
                        b.x, b.y, b.z,
                        c.x, c.y, c.z,
                    )
                )
                val p = (m.inv * Vector.unit).normalize
                val r = acos(p.dot(a))
                Circle.from(p, r)
            }
            _circumcircle = c
            return c
        }
}