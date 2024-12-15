package io.github.alxiw.punkpaging.util

object ImageUtil {
    fun makeUrl(imageName: String?): String? {
        if (imageName.isNullOrEmpty()) return imageName
        return "https://punkapi.online/v3/images/${imageName}"
    }
}
