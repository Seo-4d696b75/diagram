package jp.seo.diagram.spherical

import kotlin.math.sqrt

data class Vector(
    val x: Double,
    val y: Double,
    val z: Double,
) : Comparable<Vector> {

    companion object {
        val zero = Vector(0.0, 0.0, 0.0)
        val unit = Vector(1.0, 1.0, 1.0)
    }

    operator fun plus(other: Vector) = Vector(
        x = x + other.x,
        y = y + other.y,
        z = z + other.z,
    )

    operator fun minus(other: Vector) = Vector(
        x = x - other.x,
        y = y - other.y,
        z = z - other.z,
    )

    operator fun unaryMinus() = Vector(
        x = -x,
        y = -y,
        z = -z,
    )

    operator fun times(scalar: Double): Vector {
        require(scalar != 0.0)
        return Vector(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(scalar: Double): Vector {
        require(scalar != 0.0)
        return Vector(x / scalar, y / scalar, z / scalar)
    }

    fun dot(other: Vector) = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector) = Vector(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x,
    )

    val length: Double
        get() = sqrt(x * x + y * y + z * z)

    val normalize: Vector
        get() {
            val s = length
            return this / s
        }

    override fun compareTo(other: Vector) = compareValuesBy(
        this,
        other,
        Vector::x,
        Vector::y,
        Vector::z,
    )

    override fun toString(): String {
        return String.format("Vec(%.6f,%.6f,%.6f)", x, y, z)
    }
}
