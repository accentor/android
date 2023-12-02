package me.vanpetegem.accentor.data.artists

import java.time.Instant

data class Artist(
    val id: Int,
    val name: String,
    val normalizedName: String,
    val reviewComment: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val image: String?,
    val image500: String?,
    val image250: String?,
    val image100: String?,
    val imageType: String?,
    val fetchedAt: Instant,
) {
    fun firstCharacter() = String(intArrayOf(name.codePointAt(0)), 0, 1)

    companion object {
        fun fromDb(a: DbArtist): Artist =
            Artist(
                a.id,
                a.name,
                a.normalizedName,
                a.reviewComment,
                a.createdAt,
                a.updatedAt,
                a.image,
                a.image500,
                a.image250,
                a.image100,
                a.imageType,
                a.fetchedAt,
            )

        fun fromApi(
            a: ApiArtist,
            fetchTime: Instant,
        ) = Artist(
            a.id,
            a.name,
            a.normalizedName,
            a.reviewComment,
            a.createdAt,
            a.updatedAt,
            a.image,
            a.image500,
            a.image250,
            a.image100,
            a.imageType,
            fetchTime,
        )
    }
}
