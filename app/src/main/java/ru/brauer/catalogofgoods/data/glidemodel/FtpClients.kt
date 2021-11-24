package ru.brauer.catalogofgoods.data.glidemodel

import android.accounts.NetworkErrorException
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.BuildConfig

interface ICachedFtpClientPool {
    fun getFtpClient(): IFtpClientHolder

    interface IFtpClientHolder {
        fun connect(): Single<FTPClient>
        fun dispose()
    }
}

class CachedFtpClientPool : ICachedFtpClientPool {

    private val cacheOfClients = mutableListOf<FTPClient>()

    override fun getFtpClient(): ICachedFtpClientPool.IFtpClientHolder {
        synchronized(this) {
            return FtpClientHolder(
                if (cacheOfClients.isEmpty()) {
                    FTPClient()
                } else {
                    cacheOfClients.removeFirst()
                }
            )
        }
    }

    private fun disconnect(ftpClient: FTPClient) {
        ftpClient.completePendingCommand()
        ftpClient.logout()
        if (ftpClient.isConnected) {
            ftpClient.disconnect()
        }
        synchronized(this) {
            cacheOfClients.add(ftpClient)
        }
    }

    private fun connect(ftpClient: FTPClient): FTPClient {
        ftpClient.connect(BuildConfig.HOST_ADDRESS)
        if (ftpClient.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)) {
            throw NetworkErrorException("Can't to login on FTP server")
        }
        ftpClient.enterLocalPassiveMode()
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
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
        return ftpClient
    }

    inner class FtpClientHolder(private var ftpClient: FTPClient?) :
        ICachedFtpClientPool.IFtpClientHolder {

        override fun connect(): Single<FTPClient> =
            Single.fromCallable {
                connect(requireNotNull(ftpClient))
            }.subscribeOn(Schedulers.io())


        override fun dispose() {
            ftpClient?.run {
                disconnect(this)
            }
            ftpClient = null
        }
    }
}

