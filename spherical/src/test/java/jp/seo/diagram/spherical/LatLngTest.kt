package jp.seo.diagram.spherical

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.assertFailsWith

class LatLngTest {

    @Test
    fun compare_zero() {
        assertThat(0.0.isZero).isTrue()
        assertThat((-0.0).isZero).isTrue()
        assertThat(Double.MIN_VALUE.isZero).isTrue()
        assertThat((-Double.MIN_VALUE).isZero).isTrue()
    }

    @Test
    fun factory_method_radian() {
        assertFailsWith<IllegalArgumentException> {
            LatLng.radian(PI, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            LatLng.radian(-PI * 0.6, 0.0)
        }
        assertThat(LatLng.radian(0.0, PI).lng).isEqualTo(PI)
        assertThat(LatLng.radian(0.0, -PI).lng).isEqualTo(PI)
        assertThat(LatLng.radian(0.0, PI * 1.5).lng).isEqualTo(-PI * 0.5)
        assertThat(LatLng.radian(0.0, -PI * 2).lng.isZero).isTrue()

        val p = LatLng.radian(PI / 2, PI)
        assertThat(p.lng.isZero).isTrue()
    }

    @Test
    fun factory_method_euler() {
        assertFailsWith<IllegalArgumentException> {
            LatLng.euler(180.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            LatLng.euler(91.0, 0.0)
        }
        assertThat(LatLng.euler(0.0, 180.0).lng).isEqualTo(PI)
        assertThat(LatLng.euler(0.0, 45.0).lng).isEqualTo(PI / 4)
        assertThat(LatLng.euler(0.0, 315.0).lng).isEqualTo(-PI / 4)
        assertThat(LatLng.euler(0.0, -180.0).lng).isEqualTo(PI)
        assertThat(LatLng.euler(0.0, -360.0).lng.isZero).isTrue()
    }

    @Test
    fun equals() {
        val a = LatLng.radian(PI / 6, PI / 4)
        val b = LatLng.euler(30.0, 45.0)
        assertThat(a == b).isTrue()
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.compareTo(b)).isEqualTo(0)
    }

    @Test
    fun antipode() {
        val a = LatLng.radian(PI / 6, PI * 3 / 4)
        val b = a.antipode
        assertThat(b.lat).isEqualTo(-PI / 6)
        assertThat(b.lng).isEqualTo(-PI / 4)
    }

    @Test
    fun vector() {
        val p1 = LatLng.radian(PI / 4, PI / 6)
        val v1 = p1.vec
        val v2 = Vector(
            sqrt(3.0 / 8),
            sqrt(1.0 / 8),
            sqrt(1.0 / 2),
        )
        assertThat((v1 - v2).isZero).isTrue()
        val p2 = LatLng.vec(v1)
        assertThat((p1.lat - p2.lat).isZero).isTrue()
        assertThat((p1.lng - p2.lng).isZero).isTrue()
    }

    @Test
    fun onLine() {
        val p1 = LatLng.euler(0.0, 0.0)
        val p2 = LatLng.euler(30.0, 0.0)
        val p3 = LatLng.euler(60.0, 0.0)
        val p4 = LatLng.euler(45.0, 90.0)
        val p5 = LatLng.euler(-45.0, -90.0)
        assertThat(LatLng.onLine(p1, p2, p3)).isTrue()
        assertThat(LatLng.onLine(p1, p4, p5)).isTrue()
        assertThat(LatLng.onLine(p1, p2, p4)).isFalse()
    }
}