package me.vanpetegem.accentor.data.plays

import java.time.Instant

data class ApiPlay(
    val id: Int,
    val playedAt: Instant,
    val trackId: Int,
    val userId: Int
)
