package me.vanpetegem.accentor.data.plays

import java.time.Instant

data class Play(
    val id: Int,
    val playedAt: Instant,
    val trackId: Int,
    val userId: Int,
    val fetchedAt: Instant,
) {
    fun toDb() = DbPlay(id, playedAt, trackId, userId, fetchedAt)

    companion object {
        fun fromApi(p: ApiPlay, fetchTime: Instant) =
            Play(
                p.id,
                p.playedAt,
                p.trackId,
                p.userId,
                fetchTime,
            )
    }
}
