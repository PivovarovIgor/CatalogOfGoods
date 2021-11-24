package ru.brauer.catalogofgoods.data.glidemodel

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import io.reactivex.rxjava3.disposables.Disposable
import java.io.InputStream
import java.util.concurrent.Executors

class FtpImageFileModelLoader(private val ftpClientPool: ICachedFtpClientPool) :
    ModelLoader<FtpModel, InputStream> {

    override fun buildLoadData(
        model: FtpModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(ObjectKey(model), FtpImageFileDataFetcher(model, ftpClientPool))
    }

    override fun handles(model: FtpModel): Boolean {
        return true
    }
}

class FtpImageFileDataFetcher(
    private val model: FtpModel,
    private val ftpClientPool: ICachedFtpClientPool
) : DataFetcher<InputStream> {

    private var ftpClientHolder: ICachedFtpClientPool.IFtpClientHolder? = null
    private var disposable: Disposable? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {

        ftpClientHolder = ftpClientPool.getFtpClient()
        disposable = ftpClientHolder?.let {
            it.connect()
                .subscribe({ ftpClient ->
                    callback.onDataReady(ftpClient.retrieveFileStream(model.fileName))
                }, { exception ->
                    callback.onLoadFailed(exception as Exception)
                })
        }
    }

    override fun cleanup() {
        ftpClientHolder?.dispose()
        ftpClientHolder = null
    }

    override fun cancel() {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}

class FtpImageFileModelLoaderFactory(private val ftpClientPool: ICachedFtpClientPool) : ModelLoaderFactory<FtpModel, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<FtpModel, InputStream> {
        return FtpImageFileModelLoader(ftpClientPool)
    }

    override fun teardown() {
        Executors.newCachedThreadPool()
    }
}
