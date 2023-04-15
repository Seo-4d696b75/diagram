package jp.seo.diagram.spherical

import kotlin.math.abs

data class Zero(
    val error: Double = Double.MIN_VALUE
)

val zero = Zero()

val Vector.isZero: Boolean
    get() = x.isZero && y.isZero && z.isZero

val Double.isZero: Boolean
    get() = compareTo(zero) == 0

operator fun Double.compareTo(zero: Zero): Int {
    if (abs(this) < zero.error) return 0
    return if (this < 0.0) -1 else 1
}
