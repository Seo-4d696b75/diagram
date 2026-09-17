package com.seo4d696b75.diagram.station

import com.seo4d696b75.diagram.station.model.Result
import com.seo4d696b75.diagram.station.model.Station
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StationDiagramTest {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun calcMatchesDiagramJson() {
        val src = readResource("station.json")
        val stations = json.decodeFromString<List<Station>>(src)
        val actual = stations.calculateDiagram()

        val dst = readResource("diagram.json")
        val expected = json.decodeFromString<Result>(dst)

        // compare as data
        Assertions.assertEquals(expected, actual)

        // compare as JSON string
        val str = json.encodeToString<Result>(actual)
        Assertions.assertEquals(dst, str)
    }

    private fun readResource(name: String): String {
        val resource = requireNotNull(javaClass.classLoader.getResource(name)) {
            "$name not found on test classpath"
        }
        return resource.readText()
    }
}