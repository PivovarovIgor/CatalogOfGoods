package ru.brauer.catalogofgoods.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.glidemodel.FtpModel

private val DRAWABLE_CROSS_FADE_FACTORY =
    DrawableCrossFadeFactory.Builder()
        .setCrossFadeEnabled(true)
        .build()
private const val ROUNDING_RADIUS_FOR_PHOTO = 25

fun ImageView.loadFirstImage(listOfPhotosUri: List<String>) =
    Glide.with(this)
        .load(listOfPhotosUri
            .firstOrNull()
            ?.let { FtpModel(it) }
        )
        .placeholder(R.drawable.ic_baseline_image_24)
        .error(R.drawable.ic_baseline_broken_image_24)
        .transition(withCrossFade(DRAWABLE_CROSS_FADE_FACTORY))
        .transform(RoundedCorners(ROUNDING_RADIUS_FOR_PHOTO))
        .into(this)