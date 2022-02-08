package ru.brauer.catalogofgoods.data.net

import android.accounts.NetworkErrorException
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import ru.brauer.catalogofgoods.extensions.convertPathOfPhotoRelativelyFileName
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import javax.inject.Inject

class CatalogOfGoodsFtpRetriever @Inject constructor(
    private val commerceMlParser: IXmlParserByRule,
    private val schedulersProvider: ISchedulerProvider
) : ICatalogOfGoodsRetrieverFromNet {

    companion object {
        private const val TIMEOUT = 10000
    }

    override fun retrieve(): Observable<List<EntityOfCommerceMl>> =
        Observable.create<List<EntityOfCommerceMl>> { emitter ->

            val ftpClient = FTPClient()
            ftpClient.connect(BuildConfig.HOST_ADDRESS)
            if (ftpClient.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)) {
                ftpClient.enterLocalActiveMode()
                ftpClient.setFileType(FTP.ASCII_FILE_TYPE)
                ftpClient.setDataTimeout(TIMEOUT)

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
                val listOfFiles = getListOfFiles(ftpClient)
                try {
                    retrieveFromEachFiles(listOfFiles, ftpClient, emitter)
                } catch (exception: Throwable) {
                    emitter.onError(exception)
                }
                ftpClient.logout()
                ftpClient.disconnect()
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