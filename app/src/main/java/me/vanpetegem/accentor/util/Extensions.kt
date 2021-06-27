package me.vanpetegem.accentor.util

fun Int?.formatTrackLength(): String? =
    if (this == null)
        "-:--"
    else
        "${this / 60}:${(this % 60).toString().padStart(2, '0')}"
