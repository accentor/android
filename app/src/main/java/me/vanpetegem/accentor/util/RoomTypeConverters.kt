package me.vanpetegem.accentor.util

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import me.vanpetegem.accentor.data.tracks.Role
import me.vanpetegem.accentor.data.users.Permission

class RoomTypeConverters {
    @TypeConverter
    fun permissionFromInt(value: Int?): Permission? {
        return when (value) {
            null -> null
            1 -> Permission.USER
            2 -> Permission.MODERATOR
            3 -> Permission.ADMIN
            else -> null
        }
    }

    @TypeConverter
    fun permissionToInt(value: Permission?): Int? {
        return when (value) {
            null -> null
            Permission.USER -> 1
            Permission.MODERATOR -> 2
            Permission.ADMIN -> 3
        }
    }

    @TypeConverter
    fun roleFromInt(value: Int?): Role? {
        return when (value) {
            null -> null
            1 -> Role.MAIN
            2 -> Role.PERFORMER
            3 -> Role.COMPOSER
            4 -> Role.CONDUCTOR
            5 -> Role.REMIXER
            6 -> Role.PRODUCER
            7 -> Role.ARRANGER
            else -> null
        }
    }

    @TypeConverter
    fun roleToInt(value: Role?): Int? {
        return when (value) {
            null -> null
            Role.MAIN -> 1
            Role.PERFORMER -> 2
            Role.COMPOSER -> 3
            Role.CONDUCTOR -> 4
            Role.REMIXER -> 5
            Role.PRODUCER -> 6
            Role.ARRANGER -> 7
        }
    }

    @TypeConverter
    fun localDateFromString(value: String?): LocalDate? {
        value ?: return null
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? {
        value ?: return null
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun instantFromString(value: String?): Instant? {
        value ?: return null
        return Instant.parse(value)
    }

    @TypeConverter
    fun instantToString(value: Instant?): String? {
        value ?: return null
        return value.toString()
    }
}
