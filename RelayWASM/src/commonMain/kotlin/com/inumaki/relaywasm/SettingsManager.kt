package com.inumaki.relaywasm

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.file.TomlFileReader
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlBasicString
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlBoolean
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlDouble
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlLong
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class Settings(
    val module: ModuleSettings,
    val group: List<Group>
)

@Serializable
data class ModuleSettings(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val version: String
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val setting: List<GroupSetting>
)

@Serializable
data class GroupSetting(
    val key: String,
    val type: String,
    val label: String,
    val description: String? = null,
    @Serializable(with = AnyTomlSerializer::class)
    val default: Any
)

object AnyTomlSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AnyToml", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> encoder.encodeString(value)
            is Boolean -> encoder.encodeBoolean(value)
            is Int -> encoder.encodeInt(value)
            is Double -> encoder.encodeDouble(value)
            else -> error("Unsupported type: $value")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Any {
        return when (decoder) {
            is com.akuleshov7.ktoml.decoders.TomlMainDecoder -> {
                val element = decoder.decodeValue()
                println("${element.javaClass}: ${element}")
                when (element) {
                    is TomlBasicString -> element
                    is TomlBoolean -> element
                    is TomlDouble -> element
                    is String -> element
                    is Boolean -> element
                    is ArrayList<*> -> element
                    else -> error("Unsupported TOML value: $element")
                }
            }
            else -> error("Unsupported decoder: $decoder")
        }
    }
}

object SettingsManager {
    var settings: Settings? = null
    var values: Map<Pair<String, String>, Any> = emptyMap()

    fun loadSettings(): Settings {
        val decoded = TomlFileReader(
            inputConfig = TomlInputConfig(
                ignoreUnknownNames = true
            )
        ).decodeFromFile<Settings>(serializer(), "C://Users/kempc/development/rust/module_test/target/wasm32-unknown-unknown/release/Settings.toml")

        settings = decoded

        return decoded
    }

    fun getSettingInGroup(groupId: String, key: String): Any? {
        // first check values
        return values[groupId to key] ?: settings?.group?.first {
            it.id == groupId
        }?.setting?.first {
            it.key == key
        }?.default
    }

    fun setSettingInGroup(groupId: String, key: String, value: Any) {

    }
}