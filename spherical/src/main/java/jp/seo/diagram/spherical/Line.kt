package jp.seo.diagram.spherical

import kotlin.math.abs

val Vector.unitNormal: Vector
    get() {
        val n = if (abs(z) == 0.0) {
            if (y > 0 || (abs(y) == 0.0 && x > 0)) this else -this
        } else if (z > 0) this else -this
        return n.normalize
    }

class Line private constructor(
    /**
     * 単位法線ベクトル
     *
     * 直線には向きが無いため単位法線ベクトルは反対向きを含め二つ存在しますが、
     * ベクトルに対応する緯度・経度に関して以下の条件で正規化します
     *
     * `0 <= lat <= π/2 , 0 <= lng < π`
     */
    val normal: Vector
) : Comparable<Line> {

    companion object {
        fun normal(normal: Vector) = Line(normal.unitNormal)

        fun normal(n: LatLng) = normal(n.vec)

        fun between(a: LatLng, b: LatLng): Line {
            if (a == b) throw IllegalArgumentException("Line can't be determined from duplicated points")
            if (a.antipode == b) throw IllegalArgumentException("pair of antipodes can't determine a line")
            return normal(a.vec.cross(b.vec))
        }

        fun between(a: Vector, b: Vector): Line {
            if (a == b) throw IllegalArgumentException("Line can't be determined from duplicated points")
            if (-a == b) throw IllegalArgumentException("pair of antipodes can't determine a line")
            return normal(a.cross(b))
        }

        fun onLine(a: Vector, b: Vector, c: Vector): Boolean {
            return a.cross(b).dot(c).isZero
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        return other is Line
                && normal == other.normal
    }

    override fun hashCode() = normal.hashCode()

    override fun toString() = "Line(normal=$normal)"

    override fun compareTo(other: Line) = normal.compareTo(other.normal)

    fun intersection(other: Line): Vector {
        val i = normal.cross(other.normal)
        if (i.isZero) throw IllegalArgumentException("line duplicated")
        return i.unitNormal
    }

    fun onSameSide(a: Vector, b: Vector): Boolean {
        return normal.dot(a) * normal.dot(b) >= zero
    }
}