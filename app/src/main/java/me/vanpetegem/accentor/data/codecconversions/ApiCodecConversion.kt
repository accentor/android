package me.vanpetegem.accentor.data.codecconversions

data class ApiCodecConversion(
    val id: Int,
    val name: String,
    val ffmpegParams: String,
    val resultingCodecId: Int
)
