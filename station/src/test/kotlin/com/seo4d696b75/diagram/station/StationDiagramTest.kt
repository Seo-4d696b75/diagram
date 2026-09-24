package com.seo4d696b75.diagram.station

import com.seo4d696b75.diagram.station.model.Result
import com.seo4d696b75.diagram.station.model.Station
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StationDiagramTest {

    @Test
    fun calcMatchesDiagramJson() {
        val stations = json.decodeFromString<List<Station>>(
            readResource("station.json"),
        )
        val actual = stations.calculateDiagram()

        val dst = readResource("diagram.json")
        val expected = json.decodeFromString<Result>(dst)

        // compare as data
        Assertions.assertEquals(expected, actual)

        // compare as JSON string
        val str = json.encodeToString<Result>(actual)
        Assertions.assertEquals(dst, str)
    }
}