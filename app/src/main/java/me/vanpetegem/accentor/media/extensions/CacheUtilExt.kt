package me.vanpetegem.accentor.media.extensions

import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.cache.ContentMetadata.KEY_CONTENT_LENGTH
import me.vanpetegem.accentor.audioCache

fun isCached(mediaId: String): Boolean {
    val length =
        audioCache.getContentMetadata(mediaId).get(KEY_CONTENT_LENGTH, C.LENGTH_UNSET.toLong())
    Log.d("CACHE_UTIL", "$mediaId length: $length")
    if (length == C.LENGTH_UNSET.toLong()) return false
    Log.d("CACHE_UTIL", "$mediaId cached: ${audioCache.getCachedLength(mediaId, 0, length)}")
    return length == audioCache.getCachedLength(mediaId, 0, length)

}