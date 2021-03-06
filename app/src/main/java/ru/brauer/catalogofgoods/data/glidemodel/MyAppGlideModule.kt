package ru.brauer.catalogofgoods.data.glidemodel

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            FtpModel::class.java, InputStream::class.java,
            FtpImageFileModelLoaderFactory(CachedFtpClientPool())
        )
    }
}