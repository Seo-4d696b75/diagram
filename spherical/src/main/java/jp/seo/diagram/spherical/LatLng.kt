package jp.seo.diagram.spherical

import java.util.*
import kotlin.math.*

val Double.radian: Double
    get() = this * PI / 180

val Double.euler: Double
    get() = this * 180 / PI

val Double.normalizeLng: Double
    get() {
        var v = this
        while (v > PI) v -= PI * 2
        while (v <= -PI) v += PI * 2
        return v
    }

class LatLng private constructor(
    /**
     * 緯度 (rad)
     *
     * `-π/2 <= lat <= π/2`
     */
    val lat: Double,

    /**
     * 経度 (rad)
     *
     * - 経度の表現は±2nπの冗長性がありますが、`-π < lng <= π`の範囲で正規化します
     * - 極（lat=±π/2）の場合は`lng = 0`で固定です
     */
    val lng: Double,
) : Comparable<LatLng> {

    companion object {
        fun radian(lat: Double, lng: Double): LatLng {
            require(lat in -PI / 2..PI / 2)
            return if (abs(lat) == PI / 2) {
                LatLng(lat, 0.0)
            } else {
                LatLng(lat, lng.normalizeLng)
            }
        }

        fun euler(lat: Double, lng: Double): LatLng {
            return radian(
                lat = lat.radian,
                lng = lng.radian,
            )
        }

        fun vec(v: Vector): LatLng {
            val lng = atan2(v.y, v.x)
            val r = sqrt(v.x * v.x + v.y * v.y)
            val lat = atan2(v.z, r)
            return radian(lat, lng)
        }

        fun onLine(a: LatLng, b: LatLng, c: LatLng): Boolean {
            val n1 = a.vec.cross(b.vec)
            val n2 = b.vec.cross(c.vec)
            return abs(n1.cross(n2).length).isZero
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        return other is LatLng
                && lat == other.lat
                && lng == other.lng
    }

    override fun hashCode() = Objects.hash(lat, lng)

    override fun toString() = "LatLng(${lat.euler}, ${lng.euler})"

    override fun compareTo(other: LatLng) = compareValuesBy(
        this,
        other,
        LatLng::lat,
        LatLng::lng,
    )

    fun copy(lat: Double = this.lat, lng: Double = this.lng) = radian(lat, lng)

    fun lat(lat: Double) = radian(lat, this.lng)

    fun lng(lng: Double) = radian(this.lat, lng)

    val antipode: LatLng
        get() = radian(
            lat = -this.lat,
            lng = this.lng + PI,
        )

    val vec: Vector
        get() {
            val r = cos(lat)
            return Vector(
                x = r * cos(lng),
                y = r * sin(lng),
                z = sin(lat)
            )
        }

    fun between(other: LatLng) = acos(vec.dot(other.vec))
}