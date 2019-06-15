package me.vanpetegem.accentor.util

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.response
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import me.vanpetegem.accentor.data.users.Permission
import java.io.Reader
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

inline fun <reified T : Any> Request.responseObject() =
    response(gsonDeserializer<T>())


inline fun <reified T : Any> gsonDeserializer() = object : ResponseDeserializable<T> {
    override fun deserialize(reader: Reader): T? = gsonObject().fromJson<T>(reader, object : TypeToken<T>() {}.type)
}

inline fun <reified T : Any> Request.jsonBody(src: T) =
    this.jsonBody(
        gsonObject().toJson(src, object : TypeToken<T>() {}.type)
            .also { Fuel.trace { "serialized $it" } } as String
    )

fun gsonObject(): Gson {
    val builder = GsonBuilder()
    builder.setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    builder.registerTypeAdapter(Permission::class.java, object : TypeAdapter<Permission>() {
        override fun write(out: JsonWriter, value: Permission) {
            out.value(value.name.toLowerCase())
        }

        override fun read(`in`: JsonReader): Permission {
            return Permission.valueOf(`in`.nextString().toUpperCase())
        }
    })

    builder.registerTypeAdapter(LocalDate::class.java, object : TypeAdapter<LocalDate>() {
        override fun write(out: JsonWriter, value: LocalDate?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
        }

        override fun read(`in`: JsonReader): LocalDate? {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return null
            }
            return LocalDate.parse(`in`.nextString(), DateTimeFormatter.ISO_LOCAL_DATE)
        }
    })

    builder.registerTypeAdapter(Instant::class.java, object : TypeAdapter<Instant>() {
        override fun write(out: JsonWriter, value: Instant?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.toString())
            }
        }

        override fun read(`in`: JsonReader): Instant? {
            if (`in`.peek() == JsonToken.NULL) {
                `in`.nextNull()
                return null
            }
            return Instant.parse(`in`.nextString())
        }
    })

    return builder.create()
}