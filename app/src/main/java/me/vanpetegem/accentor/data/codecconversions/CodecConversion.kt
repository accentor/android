package me.vanpetegem.accentor.data.codecconversions

data class CodecConversion(
    val id: Int,
    val name: String,
    val ffmpegParams: String,
    val resultingCodecId: Int,
)
