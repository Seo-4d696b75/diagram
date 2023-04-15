package jp.seo.diagram.spherical

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.test.assertFailsWith

class TriangleTest {

    @Test
    fun factory_method() {
        val p1 = LatLng.euler(0.0, 0.0).vec
        val p2 = LatLng.euler(0.0, 150.0).vec
        val p3 = LatLng.euler(60.0, 0.0).vec

        val t = Triangle.points(p1, p2, p3)
        assertThat(t.a).isEqualTo(p2)
        assertThat(t.b).isEqualTo(p3)
        assertThat(t.c).isEqualTo(p1)

        assertFailsWith<IllegalArgumentException> {
            Triangle.points(p1, p1, p2)
        }
        assertFailsWith<java.lang.IllegalArgumentException> {
            Triangle.points(p1, p2, -p1)
        }
    }

    @Test
    fun contains() {
        val p1 = LatLng.euler(0.0, 0.0).vec
        val p2 = LatLng.euler(0.0, 90.0).vec
        val p3 = LatLng.euler(60.0, 0.0).vec
        val t = Triangle.points(p1, p2, p3)

        val p4 = LatLng.euler(10.0, 20.0).vec
        val p5 = LatLng.euler(0.0, 45.0).vec
        val p6 = LatLng.euler(60.0, 1.0).vec

        assertThat(t.contains(p1)).isTrue()
        assertThat(t.contains(p2)).isTrue()
        assertThat(t.contains(p3)).isTrue()
        assertThat(t.contains(p4)).isTrue()
        assertThat(t.contains(p5)).isTrue()
        assertThat(t.contains(p6)).isFalse()
    }

    @Test
    fun circle() {
        val p1 = LatLng.euler(60.0, 0.0).vec
        val p2 = LatLng.euler(60.0, 120.0).vec
        val p3 = LatLng.euler(60.0, -120.0).vec
        val t = Triangle.points(p1, p2, p3)

        val c = t.circumcircle
        assertThat(c.center.minus(Vector(0.0, 0.0, 1.0)).isZero).isTrue()
        assertThat(c.radius.minus(PI / 6).isZero).isTrue()
    }
}