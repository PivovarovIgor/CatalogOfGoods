package ru.brauer.catalogofgoods.data.net

import android.accounts.NetworkErrorException
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.data.Goods
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import java.io.InputStream
import javax.inject.Inject

class CatalogOfGoodsFtpRetriever @Inject constructor(
    private val commerceMlParser: IXmlParserByRule
) : ICatalogOfGoodsRetrieverFromNet {

    override fun retrieve(): List<Goods> {

        val ftpClient = FTPClient()
        ftpClient.connect(BuildConfig.HOST_ADDRESS)
        var result = listOf<Goods>()
        if (ftpClient.login(BuildConfig.LOGIN, BuildConfig.PASSWORD)) {
            ftpClient.enterLocalActiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

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

            val fileName = "goods/1/import___95ccdeb9-1dbb-472a-a773-83e7ceedd818.xml"
            val inputStream: InputStream = ftpClient.retrieveFileStream(fileName)
                ?: throw NetworkErrorException("Not found file '$fileName'")

            result = commerceMlParser.parse(inputStream)

            ftpClient.logout()
            ftpClient.disconnect()
        }
        return result
    }
}