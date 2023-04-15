package jp.seo.diagram.spherical

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LineTest {

    @Test
    fun factory_method_normal() {
        val l1 = Line.normal(LatLng.euler(-30.0, 0.0))
        assertThat((l1.normal - LatLng.euler(30.0, 180.0).vec).isZero).isTrue()
        val l2 = Line.normal(LatLng.euler(0.0, -60.0))
        assertThat((l2.normal - LatLng.euler(0.0, 120.0).vec).isZero).isTrue()
    }

    @Test
    fun factory_method_between() {
        val l1 = Line.between(
            LatLng.euler(-30.0, 0.0),
            LatLng.euler(60.0, 0.0),
        )
        assertThat((l1.normal - LatLng.euler(0.0, 90.0).vec).isZero).isTrue()
    }

    @Test
    fun intersection() {
        val l1 = Line.normal(LatLng.euler(-30.0, 0.0))
        val l2 = Line.normal(LatLng.euler(0.0, 90.0))
        val i1 = l1.intersection(l2)
        val i2 = LatLng.euler(60.0, 0.0).vec.unitNormal
        assertThat(i1.minus(i2).isZero).isTrue()
    }

    @Test
    fun onSameSide() {
        val line = Line.normal(LatLng.euler(-30.0, 0.0))
        val p1 = LatLng.euler(-30.0, 0.0).vec
        val p2 = LatLng.euler(0.0, 0.0).vec
        val p3 = LatLng.euler(60.0, 0.0).vec
        val p4 = LatLng.euler(60.0, 180.0).vec
        assertThat(line.onSameSide(p1, p2)).isTrue()
        assertThat(line.onSameSide(p1, p3)).isTrue()
        assertThat(line.onSameSide(p3, p4)).isTrue()
        assertThat(line.onSameSide(p2, p4)).isFalse()
    }
}