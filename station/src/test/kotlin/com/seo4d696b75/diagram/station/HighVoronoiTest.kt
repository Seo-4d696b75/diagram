@file:Suppress("NonAsciiCharacters", "RemoveRedundantBackticks")

package com.seo4d696b75.diagram.station

import com.seo4d696b75.diagram.station.model.GeoJsonFeature
import com.seo4d696b75.diagram.station.model.GeoJsonGeometry
import com.seo4d696b75.diagram.station.model.HighVoronoiStation
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HighVoronoiTest {

    /**
     * 次数
     *
     * 現時点での最大レーダー駅数は20を想定
     * - 基本値（電友0人）	2
     * - 電友ボーナス（20人アクティブ上限）	+10
     * - イベント補正	+2
     * - なつめスキル	+2
     * - レーダーブースター	+4
     */
    private val highVoronoiLevel = 20

    private lateinit var stations: List<HighVoronoiStation>

    /**
     * [com.seo4d696b75.diagram.station.model.Result] を入力として使う
     */
    @Serializable
    private data class Input(
        @SerialName("node_list")
        val stations: List<HighVoronoiStation>
    )

    @BeforeEach
    fun setup() {
        val string = readResource("diagram.json")
        val input = json.decodeFromString<Input>(string)
        stations = input.stations
    }

    @ParameterizedTest(name = "{0}({1})")
    @CsvSource(
        "東京, 100201",
        "外周部1_那覇空港, 9992701",
        "外周部2_稚内, 1111553",
        "外周部3_犬吠, 9933509",
    )
    fun `高次ボロノイの計算`(label: String, code: Int) = runTest {
        val center = stations.first { it.code == code }
        val actual = stations.calculateHighVoronoi(highVoronoiLevel, center)

        val expected = json.decodeFromString<GeoJsonFeature<GeoJsonGeometry>>(
            readResource("high_voronoi/$code.json"),
        )
        Assertions.assertEquals(expected, actual)
    }
}
