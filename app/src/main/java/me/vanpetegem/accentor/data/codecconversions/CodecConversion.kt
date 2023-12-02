package me.vanpetegem.accentor.data.codecconversions

import java.time.Instant

data class CodecConversion(
    val id: Int,
    val name: String,
    val ffmpegParams: String,
    val resultingCodecId: Int,
    val fetchedAt: Instant,
) {
    companion object {
        fun fromDb(c: DbCodecConversion) =
            CodecConversion(
                c.id,
                c.name,
                c.ffmpegParams,
                c.resultingCodecId,
                c.fetchedAt,
            )

        fun fromApi(
            c: ApiCodecConversion,
            fetchTime: Instant,
        ) = CodecConversion(
            c.id,
            c.name,
            c.ffmpegParams,
            c.resultingCodecId,
            fetchTime,
        )
    }
}
