package ru.brauer.catalogofgoods.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.glidemodel.FtpModel

fun ImageView.loadFirstImage(listOfPhotosUri: List<String>) =
    Glide.with(this)
        .load(listOfPhotosUri
            .firstOrNull()
            ?.let { FtpModel(it) }
        )
        .placeholder(R.drawable.ic_baseline_image_24)
        .error(R.drawable.ic_baseline_broken_image_24)
        .into(this)