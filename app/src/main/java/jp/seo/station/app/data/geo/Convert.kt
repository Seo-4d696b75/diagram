package jp.seo.station.app.data.geo

import jp.seo.diagram.core.Point
import jp.seo.diagram.core.VoronoiDiagram.VoronoiArea
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toFixed(digit: Int = 6): Double {
    assert(digit > 0)
    val scale = 10.0.pow(digit)
    return (this * scale).roundToInt() / scale
}

val Point.geoJson: List<Double>
    get() = listOf(x.toFixed(), y.toFixed())

object VoronoiSerializer : KSerializer<VoronoiArea> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("VoronoiArea") {
            element<String>("type")
            element<JsonElement>("geometry")
        }

    override fun deserialize(decoder: Decoder): VoronoiArea {
        throw NotImplementedError()
    }

    override fun serialize(encoder: Encoder, value: VoronoiArea) {
        require(encoder is JsonEncoder)
        val element = encoder.json.encodeToJsonElement(
            if (value.enclosed) {
                PolygonFeature.fromVoronoi(value)
            } else {
                LineStringFeature.fromVoronoi(value)
            }
        )
        encoder.encodeJsonElement(element)
    }

}