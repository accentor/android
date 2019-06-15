package me.vanpetegem.accentor.util

import androidx.room.TypeConverter
import me.vanpetegem.accentor.data.users.Permission
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RoomTypeConverters {
    @TypeConverter
    fun fromInt(value: Int?): Permission? {
        return when (value) {
            null -> {
                null
            }
            1 -> {
                Permission.USER
            }
            2 -> {
                Permission.MODERATOR
            }
            3 -> {
                Permission.ADMIN
            }
            else -> {
                null
            }
        }
    }

    @TypeConverter
    fun permissionToInt(value: Permission?): Int? {
        return when (value) {
            null -> {
                null
            }
            Permission.USER -> {
                1
            }
            Permission.MODERATOR -> {
                2
            }
            Permission.ADMIN -> {
                3
            }
        }
    }

    @TypeConverter
    fun localDateFromString(value: String?): LocalDate? {
        value ?: return null
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun stringFromLocalDate(value: LocalDate?): String? {
        value ?: return null
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun localDateTimeFromString(value: String?): Instant? {
        value ?: return null
        return Instant.parse(value)
    }

    @TypeConverter
    fun stringFromInstant(value: Instant?): String? {
        value ?: return null
        return value.toString()
    }
}