package ru.brauer.catalogofgoods.data.glidemodel

import android.accounts.NetworkErrorException
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.BuildConfig
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
        return true
    }
}

class FtpImageFileDataFetcher(private val model: FtpModel) : DataFetcher<InputStream> {

    private var inputStream: InputStream? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {

        val ftpClient = FtpClientFactory.create()

        ftpClient.retrieveFileStream(model.fileName)
            .also { inputStream = it }
            .run(callback::onDataReady)
    }

    override fun cleanup() {
        inputStream?.let {
            it.close()
        }
        inputStream = null

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

object FtpClientFactory {
    fun create(): FTPClient {
        val ftpClient = FTPClient()
        ftpClient.connect(BuildConfig.HOST_ADDRESS)
        if (ftpClient.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)) {
            ftpClient.enterLocalActiveMode()
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE)
            ftpClient.setDataTimeout(10000)

            val replyCode: Int = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect()
                throw NetworkErrorException("FTP server refused connection. Reply code $replyCode")
            }

            val workDirectory = BuildConfig.PATH
            if (!ftpClient.changeWorkingDirectory(workDirectory)) {
                ftpClient.logout()
                ftpClient.disconnect()
                throw NetworkErrorException("Not found directory '$workDirectory'")
            }
        }
        return ftpClient
    }
}

class FtpImageFileModelLoaderFactory : ModelLoaderFactory<FtpModel, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<FtpModel, InputStream> {
        return FtpImageFileModelLoader()
    }

    override fun teardown() {

    }

}