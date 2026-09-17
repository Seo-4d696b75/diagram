package com.seo4d696b75.diagram.station

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
internal val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

internal fun Any.readResource(name: String): String {
    val resource = requireNotNull(javaClass.classLoader.getResource(name)) {
        "$name not found on test classpath"
    }
    return resource.readText()
}
