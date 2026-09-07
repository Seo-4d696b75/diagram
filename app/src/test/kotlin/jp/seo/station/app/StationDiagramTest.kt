package jp.seo.station.app

import jp.seo.station.app.data.RawStation
import jp.seo.station.app.data.Result
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StationDiagramTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun calcMatchesDiagramJson() {
        val src = readResource("station.json")
        val stations = json.decodeFromString<List<RawStation>>(src)
        val actual = stations.calc()
        val dst = readResource("diagram.json")
        val expected = json.decodeFromString<Result>(dst)
        assertEquals(expected, actual)
    }

    private fun readResource(name: String): String {
        val resource = requireNotNull(javaClass.classLoader.getResource(name)) {
            "$name not found on test classpath"
        }
        return resource.readText()
    }
}
