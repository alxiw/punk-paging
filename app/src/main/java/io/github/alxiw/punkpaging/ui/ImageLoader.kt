package io.github.alxiw.punkpaging.ui

import android.widget.ImageView
import com.squareup.picasso.Picasso

class ImageLoader(private val picasso: Picasso, private val baseUrl: String) {

    fun loadImage(target: ImageView, imageName: String) {
        picasso
            .load("$baseUrl${imageName}")
            .fit().centerInside()
            .into(target)
    }
}

fun ImageView.load(loader: ImageLoader, image: String) {
    loader.loadImage(this, image)
}
