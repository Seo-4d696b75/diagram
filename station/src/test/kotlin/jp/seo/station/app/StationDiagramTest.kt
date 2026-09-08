package jp.seo.station.app

import jp.seo.station.app.data.Result
import jp.seo.station.app.data.Station
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
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
        assertEquals(expected, actual)

        // compare as JSON string
        val str = json.encodeToString<Result>(actual)
        assertEquals(dst, str)
    }

    private fun readResource(name: String): String {
        val resource = requireNotNull(javaClass.classLoader.getResource(name)) {
            "$name not found on test classpath"
        }
        return resource.readText()
    }
}
