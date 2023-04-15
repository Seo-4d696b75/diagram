package jp.seo.diagram.spherical

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.test.assertFailsWith

class EdgeTest {

    @Test
    fun factory_method() {
        val p1 = LatLng.euler(-30.0, 0.0).vec
        val p2 = LatLng.euler(0.0, 90.0).vec
        val l1 = Line.between(p1, p2)
        val l2 = Line.between(p2, p1)
        assertThat(l1).isEqualTo(l2)

        assertFailsWith<IllegalArgumentException> {
            Line.between(p1, p1)
        }
        assertFailsWith<IllegalArgumentException> {
            Line.between(p1, -p1)
        }
    }

    @Test
    fun length() {
        val p1 = LatLng.euler(-30.0, 0.0).vec
        val p2 = LatLng.euler(0.0, 90.0).vec
        val e = Edge.between(p1, p2)
        assertThat(e.length).isEqualTo(PI / 2)
    }

    @Test
    fun intersection_line() {
        val p1 = LatLng.euler(0.0, -30.0).vec
        val p2 = LatLng.euler(0.0, 90.0).vec
        val e = Edge.between(p1, p2)

        val l1 = Line.normal(p2)
        val i1 = e.intersection(l1)
        val i2 = LatLng.euler(0.0, 0.0).vec
        assertThat(i1?.minus(i2)?.isZero).isTrue()

        val l2 = Line.normal(LatLng.euler(0.0, 60.0))
        val i3 = e.intersection(l2)
        val i4 = LatLng.euler(0.0, -30.0).vec
        assertThat(i3?.minus(i4)?.isZero).isTrue()

        val l3 = Line.normal(LatLng.euler(0.0, 30.0))
        val i5 = e.intersection(l3)
        assertThat(i5).isNull()
    }

    @Test
    fun intersection_edge() {
        val p1 = LatLng.euler(0.0, -30.0).vec
        val p2 = LatLng.euler(0.0, 90.0).vec
        val e1 = Edge.between(p1, p2)

        val p3 = LatLng.euler(30.0, 0.0).vec
        val p4 = LatLng.euler(-30.0, 0.0).vec
        val e2 = Edge.between(p3, p4)

        val i1 = e1.intersection(e2)
        val i2 = LatLng.euler(0.0, 0.0).vec
        assertThat(i1?.minus(i2)?.isZero).isTrue()

        val p5 = LatLng.euler(60.0, 0.0).vec
        val e3 = Edge.between(p3, p5)
        val i3 = e1.intersection(e3)
        assertThat(i3).isNull()

    }
}