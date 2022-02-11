package ru.brauer.catalogofgoods.data.net

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.apache.commons.net.ftp.FTPClient
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import ru.brauer.catalogofgoods.data.settings.FtpSettingsData
import ru.brauer.catalogofgoods.extensions.convertPathOfPhotoRelativelyFileName
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import javax.inject.Inject

class CatalogOfGoodsFtpRetriever @Inject constructor(
    private val commerceMlParser: IXmlParserByRule,
    private val schedulersProvider: ISchedulerProvider,
    private val ftpClientConnectHelperProvider: FtpClientConnectHelperProvider
) : ICatalogOfGoodsRetrieverFromNet {

    override fun retrieve(): Observable<List<EntityOfCommerceMl>> =
        Observable.create<List<EntityOfCommerceMl>> { emitter ->

            val ftpSettings = FtpSettingsData(
                hostAddress = BuildConfig.HOST_ADDRESS,
                path = BuildConfig.PATH,
                login = BuildConfig.LOGIN,
                password = BuildConfig.PASSWORD
            )

            val ftpClientConnectHelper = ftpClientConnectHelperProvider.getFtpClientConnectHelper()

            if (ftpClientConnectHelper.connect(ftpSettings)) {
                val listOfFiles = getListOfFiles(ftpClientConnectHelper.ftpClient)
                try {
                    retrieveFromEachFiles(listOfFiles, ftpClientConnectHelper.ftpClient, emitter)
                } catch (exception: Throwable) {
                    emitter.onError(exception)
                }
                ftpClientConnectHelper.disconnect()
            } else {
                ftpClientConnectHelper.throwable?.let { throw it }
            }
            emitter.onComplete()
        }.subscribeOn(schedulersProvider.io())

    private fun retrieveFromEachFiles(
        listOfFiles: List<String>,
        ftpClient: FTPClient,
        emitter: ObservableEmitter<List<EntityOfCommerceMl>>
    ) = listOfFiles
        .forEach { fileName ->
            if (emitter.isDisposed) {
                return@forEach
            }
            Log.i("12345", "file -> $fileName")
            val inputStream = ftpClient.retrieveFileStream(fileName)
                ?: throw NetworkErrorException("Not found file '$fileName' Reply code: ${ftpClient.reply}.")
            inputStream.use {
                commerceMlParser.parse(inputStream)
                    .map { it.convertPathOfPhotoRelativelyFileName(fileName) }
                    .subscribe({ emitter.onNext(it) },
                        { emitter.onError(it) },
                        { completeRetrieving(ftpClient) })
            }
        }

    private fun completeRetrieving(ftpClient: FTPClient) {
        if (!ftpClient.completePendingCommand()) {
            ftpClient.logout()
            ftpClient.disconnect()
            throw NetworkErrorException(
                "Error on complete retrieving file. Reply code: ${ftpClient.replyCode}"
            )
        }
    }

    private fun getListOfFiles(
        ftpClient: FTPClient,
        nameOfParentDir: String = ""
    ): List<String> {
        if (nameOfParentDir.count { (it == '/') } > 1) {
            return listOf()
        }
        val listOfFiles = ftpClient.listFiles(nameOfParentDir) {
            it.isDirectory || it.name.endsWith(
                ".xml",
                true
            )
        }
            ?.filterNotNull()?.toList() ?: listOf()

        val nameOfParentDirWithSlash = if (nameOfParentDir.isNotBlank()) {
            "$nameOfParentDir/"
        } else {
            ""
        }
        return listOfFiles
            .filter { it.isDirectory }
            .flatMap {
                getListOfFiles(ftpClient, "$nameOfParentDirWithSlash${it.name}")
            }.let { listOfNestedFiles ->
                listOfFiles
                    .filter { it.isFile }
                    .map { "$nameOfParentDirWithSlash${it.name}" } + listOfNestedFiles
            }
    }
}