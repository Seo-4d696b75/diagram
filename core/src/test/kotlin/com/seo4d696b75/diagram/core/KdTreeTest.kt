@file:Suppress("NonAsciiCharacters")

package com.seo4d696b75.diagram.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

class KdTreeTest {

    private fun KdTree<Point>.searchPoints(query: Point, k: Int): List<Point> =
        search(query, k).neighbors.map { it.point }

    @Test
    fun `最近傍点が先に見つかっても k 個の近傍点を返す`() {
        // root (1,0) がクエリと一致し距離0で最初に見つかる
        val points = listOf(BasePoint(0.0, 0.0), BasePoint(1.0, 0.0), BasePoint(2.0, 0.0))
        val tree = KdTree<Point>(points)

        val actual = tree.searchPoints(BasePoint(1.0, 0.0), 2)

        Assertions.assertEquals(2, actual.size)
        Assertions.assertEquals(BasePoint(1.0, 0.0), actual[0])
    }

    @Test
    fun `近傍点が k 未満の間は枝切りしない`() {
        // root (10,0) と右の子 (11,0) だけ見つかった時点で左の子 (0,0) を枝切りしてはいけない
        val points = listOf(BasePoint(0.0, 0.0), BasePoint(10.0, 0.0), BasePoint(11.0, 0.0))
        val tree = KdTree<Point>(points)

        val actual = tree.searchPoints(BasePoint(10.6, 0.0), 3)

        Assertions.assertEquals(
            listOf(BasePoint(11.0, 0.0), BasePoint(10.0, 0.0), BasePoint(0.0, 0.0)),
            actual,
        )
    }

    @ParameterizedTest(name = "k={0}")
    @ValueSource(ints = [0, -1])
    fun `k が正でない場合は例外`(k: Int) {
        val tree = KdTree<Point>(listOf(BasePoint(0.0, 0.0)))

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            tree.search(BasePoint(0.0, 0.0), k)
        }
    }

    @ParameterizedTest(name = "k={0}")
    @ValueSource(ints = [1, 2, 3, 5, 10, 20])
    fun `全探索の結果と一致する`(k: Int) {
        val random = Random(k)
        val points = List(200) { BasePoint(random.nextDouble(), random.nextDouble()) }
        val tree = KdTree<Point>(points)

        repeat(100) {
            val query = BasePoint(random.nextDouble(-0.2, 1.2), random.nextDouble(-0.2, 1.2))
            val expected = points
                .map { it to it.measure(query) }
                .sortedBy { it.second }
                .take(k)
                .map { it.second }
            val actual = tree.search(query, k).neighbors.map { Math.sqrt(it.sqrtDist) }

            Assertions.assertEquals(k, actual.size)
            expected.zip(actual).forEach { (e, a) ->
                Assertions.assertEquals(e, a, 1e-9)
            }
        }
    }
}
