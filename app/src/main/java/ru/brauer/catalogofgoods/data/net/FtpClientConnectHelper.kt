package ru.brauer.catalogofgoods.data.net

import android.accounts.NetworkErrorException
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.data.settings.FtpSettingsData

class FtpClientConnectHelper {

    companion object {
        private const val TIMEOUT = 10000
    }

    val ftpClient = FTPClient()
    private var isLogined: Boolean = false
    var throwable: Throwable? = null
        private set
    var badHostName = false
        private set
    var badLoginParams = false
        private set
    var badWorkDirectory = false
        private set

    fun connect(ftpSettingsData: FtpSettingsData): Boolean {
        initBeforeConnect()
        try {
            ftpClient.connect(ftpSettingsData.hostAddress)
        } catch (ex: Throwable) {
            throwable = ex
            badHostName = true
            return false
        }

        if (ftpSettingsData.login.isNotBlank()) {
            isLogined = try {
                ftpClient.login(ftpSettingsData.login, ftpSettingsData.password)
            } catch (ex: Throwable) {
                throwable = ex
                false
            }
            if (!isLogined) {
                badLoginParams = true
                return false
            }
        }

        ftpClient.enterLocalActiveMode()
        ftpClient.setFileType(FTP.ASCII_FILE_TYPE)
        ftpClient.setDataTimeout(TIMEOUT)

        val replyCode: Int = ftpClient.replyCode
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            disconnect()
            throwable =
                NetworkErrorException("FTP server refused connection. Reply code $replyCode")
            return false
        }

        val workDirectory = ftpSettingsData.path
        if (!ftpClient.changeWorkingDirectory(workDirectory)) {
            disconnect()
            throwable = NetworkErrorException("Not found directory '$workDirectory'")
            badWorkDirectory = true
            return false
        }

        return true
    }

    fun disconnect() {
        if (ftpClient.isConnected) {
            if (isLogined) {
                ftpClient.logout()
            }
            ftpClient.disconnect()
        }
    }

    private fun initBeforeConnect() {
        isLogined = false
        throwable = null
        badHostName = false
        badLoginParams = false
        badWorkDirectory = false
    }
}

class FtpClientConnectHelperProvider {
    fun getFtpClientConnectHelper() = FtpClientConnectHelper()
}