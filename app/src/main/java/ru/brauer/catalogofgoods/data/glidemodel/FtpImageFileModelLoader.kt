package ru.brauer.catalogofgoods.data.glidemodel

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class FtpImageFileModelLoader : ModelLoader<FtpModel, InputStream> {
    override fun buildLoadData(
        model: FtpModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), FtpImageFileDataFetcher(model))
    }

    override fun handles(model: FtpModel): Boolean {
        return model.ftpClient.listFiles(model.fileName).isNotEmpty()
    }
}

class FtpImageFileDataFetcher(private val model: FtpModel) : DataFetcher<InputStream> {

    private var inputStream: InputStream? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        model.ftpClient.retrieveFileStream(model.fileName)
            .also { inputStream = it }
            .run(callback::onDataReady)
    }

    override fun cleanup() {
        inputStream?.let {
            it.close()
        }
        inputStream = null
        model.ftpClient.completePendingCommand()
    }

    override fun cancel() {

    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(FtpModel::class.java, InputStream::class.java, FtpImageFileModelLoaderFactory())
    }
}

class FtpImageFileModelLoaderFactory : ModelLoaderFactory<FtpModel, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<FtpModel, InputStream> {
        return FtpImageFileModelLoader()
    }

    override fun teardown() {

    }

}