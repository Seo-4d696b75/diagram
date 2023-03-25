package jp.seo.station.app

import jp.seo.station.app.data.Station
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) {
    require(args.size >= 2)
    calc(args[0], args[1])
}

private fun calc(srcFile: String, dstFile: String){
    val json = Json { ignoreUnknownKeys = true }
    val src = File(srcFile).readText()
    val stations = json.decodeFromString<List<Station>>(src)
    println("station size: ${stations.size}")
}
