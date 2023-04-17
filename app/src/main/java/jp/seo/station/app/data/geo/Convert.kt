package jp.seo.station.app.data.geo

import jp.seo.station.app.data.Voronoi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.encodeToJsonElement

object VoronoiSerializer : KSerializer<Voronoi> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("VoronoiArea") {
            element<String>("type")
            element<JsonElement>("geometry")
            element<JsonElement>("properties")
        }

    override fun deserialize(decoder: Decoder): Voronoi {
        throw NotImplementedError()
    }

    override fun serialize(encoder: Encoder, value: Voronoi) {
        require(encoder is JsonEncoder)
        val element = if (value.enclosed) {
            encoder.json.encodeToJsonElement(
                PolygonFeature.fromVoronoi(value)
            )
        } else {
            encoder.json.encodeToJsonElement(
                LineStringFeature.fromVoronoi(value)
            )
        }
        encoder.encodeJsonElement(element)
    }

}