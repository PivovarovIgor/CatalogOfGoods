package ru.brauer.catalogofgoods.data.net

import android.accounts.NetworkErrorException
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.data.commerceml.IXmlParserByRule
import ru.brauer.catalogofgoods.data.entities.Goods
import java.io.InputStream
import javax.inject.Inject

class CatalogOfGoodsFtpRetriever @Inject constructor(
    private val commerceMlParser: IXmlParserByRule
) : ICatalogOfGoodsRetrieverFromNet {

    companion object {
        private const val DIR_NAME_TO_IGNORE = "import_files"
    }

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
            val regexOfGoods = Regex("goods/(.*)\\d/(.*)import\\d*___(.*)")
            val listOfFiles = getListOfFiles(ftpClient)
            result = listOfFiles
                .filter { it.matches(regexOfGoods) }
                .map { fileName ->
                    val inputStream: InputStream = ftpClient.retrieveFileStream(fileName)
                        ?: throw NetworkErrorException("Not found file '$fileName'")
                    commerceMlParser.parse(inputStream).also {
                        inputStream.close()
                        if (!ftpClient.completePendingCommand()) {
                            ftpClient.logout()
                            ftpClient.disconnect()
                            throw NetworkErrorException(
                                "Error on complete retrieving file. Reply code: ${ftpClient.replyCode}"
                            )
                        }
                    }
                }.flatten()

            ftpClient.logout()
            ftpClient.disconnect()
        }
        return result
    }

    private fun getListOfFiles(ftpClient: FTPClient, nameOfParentDir: String = ""): List<String> {
        val listOfFiles = ftpClient.listFiles(nameOfParentDir) { it.name != DIR_NAME_TO_IGNORE }
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