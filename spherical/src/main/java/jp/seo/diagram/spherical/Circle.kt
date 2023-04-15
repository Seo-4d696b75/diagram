package jp.seo.diagram.spherical

import kotlin.math.PI
import kotlin.math.acos

class Circle private constructor(
    val center: Vector,
    val radius: Double,
) {
    companion object {
        fun from(center: Vector, radius: Double): Circle {
            require(0.0 < radius && radius < PI)
            return if (radius <= PI / 2) {
                Circle(center, radius)
            } else {
                Circle(-center, PI - radius)
            }
        }
    }

    fun contains(p: Vector): Boolean {
        val d = acos(p.dot(center))
        return d - radius <= zero
    }
}