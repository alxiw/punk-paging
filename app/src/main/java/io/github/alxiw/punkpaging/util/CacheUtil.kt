package io.github.alxiw.punkpaging.util

import android.content.Context
import android.os.StatFs
import java.io.File

private const val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024 // 5MB
private const val MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024 // 100MB
private const val CACHE_NAME = "punkpaging"

object CacheUtil {

    fun createDefaultCacheDir(context: Context): File {
        val cache = File(context.applicationContext.cacheDir, CACHE_NAME)
        if (!cache.exists()) {
            cache.mkdirs()
        }
        return cache
    }

    fun calculateDiskCacheSize(dir: File): Long {
        var size = MIN_DISK_CACHE_SIZE.toLong()

        try {
            val statFs = StatFs(dir.absolutePath)
            val blockCount = statFs.blockCountLong
            val blockSize = statFs.blockSizeLong
            val available = blockCount * blockSize
            // Target 2% of the total space
            size = available / 50
        } catch (ignored: IllegalArgumentException) { /* do nothing */ }

        return size.coerceIn(MIN_DISK_CACHE_SIZE.toLong(), MAX_DISK_CACHE_SIZE.toLong())
    }
}
