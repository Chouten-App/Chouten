package com.inumaki.relaywasm.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable(with = RustResultSerializer::class)
sealed class RustResult<out T, out E> {
    data class Ok<T>(val value: T) : RustResult<T, Nothing>()
    data class Err<E>(val error: E) : RustResult<Nothing, E>()
}

class RustResultSerializer<T, E>(
    private val valueSerializer: KSerializer<T>,
    private val errorSerializer: KSerializer<E>
) : KSerializer<RustResult<T, E>> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("RustResult")

    override fun serialize(encoder: Encoder, value: RustResult<T, E>) {
        require(encoder is JsonEncoder)
        val json = when (value) {
            is RustResult.Ok -> buildJsonObject {
                put("Ok", encoder.json.encodeToJsonElement(valueSerializer, value.value))
            }
            is RustResult.Err -> buildJsonObject {
                put("Err", encoder.json.encodeToJsonElement(errorSerializer, value.error))
            }
        }
        encoder.encodeJsonElement(json)
    }

    override fun deserialize(decoder: Decoder): RustResult<T, E> {
        require(decoder is JsonDecoder)
        val json = decoder.decodeJsonElement().jsonObject
        return when {
            "Ok" in json -> RustResult.Ok(decoder.json.decodeFromJsonElement(valueSerializer, json["Ok"]!!))
            "Err" in json -> RustResult.Err(decoder.json.decodeFromJsonElement(errorSerializer, json["Err"]!!))
            else -> error("Invalid RustResult JSON: $json")
        }
    }
}