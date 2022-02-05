package ru.brauer.catalogofgoods.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.glidemodel.FtpModel

private const val TRANSITION_IMAGE_APPEARANCE_DURATION = 180
private const val ROUNDING_RADIUS_FOR_PHOTO = 25

private val DRAWABLE_CROSS_FADE_FACTORY =
    DrawableCrossFadeFactory.Builder(TRANSITION_IMAGE_APPEARANCE_DURATION)
        .setCrossFadeEnabled(true)
        .build()

fun ImageView.loadFirstImage(listOfPhotosUri: List<String>) =
    loadFirstImage(listOfPhotosUri.firstOrNull())

fun ImageView.loadFirstImage(listOfPhotosUri: String?) =
    Glide.with(this)
        .load(
            FtpModel(listOfPhotosUri ?: "")
        )
        .placeholder(R.drawable.ic_baseline_image_24)
        .error(R.drawable.ic_baseline_broken_image_24)
        .transition(withCrossFade(DRAWABLE_CROSS_FADE_FACTORY))
        .transform(RoundedCorners(ROUNDING_RADIUS_FOR_PHOTO))
        .into(this)