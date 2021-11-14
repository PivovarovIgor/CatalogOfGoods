package ru.brauer.catalogofgoods.data

import android.accounts.NetworkErrorException
import android.util.Xml
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.xmlpull.v1.XmlPullParser
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.domain.IRepository
import java.io.InputStream

class MockRepository : IRepository {
    override fun getGoods(): Single<List<Goods>> = Single.fromCallable {

        Thread.sleep(2000L)
        listOf(
            Goods(
                "Фартук",
                "https://blondypro.ru/upload/iblock/f48/f48036173043a663915d9893a128e497.png"
            ),
            Goods("Рубашка", ""),
            Goods("Штаны", ""),
        )
    }.subscribeOn(Schedulers.io())
}

class CatalogOfGoodsRepository : IRepository {
    override fun getGoods(): Single<List<Goods>> =
        Single.fromCallable {
            CatalogOfGoodsFtpRetriever().retrieveFromFtp()
        }.subscribeOn(Schedulers.io())
}

class CatalogOfGoodsFtpRetriever {
    fun retrieveFromFtp(): List<Goods> {

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

            val commerceMlParser = CommerceMlParser()
            result = commerceMlParser.parse(inputStream)

            ftpClient.logout()
            ftpClient.disconnect()
        }
        return result
    }
}

class CommerceMlParser {

    companion object {
        private val xmlNamespace: String? = null
        private const val TAG_COMMERCE_INFO = "КоммерческаяИнформация"
        private const val TAG_CATALOG = "Каталог"
        private const val TAG_GOODS_MULTITUDE = "Товары"
        private const val TAG_GOODS = "Товар"
        private const val TAG_ID = "Ид"
        private const val TAG_NAME = "Наименование"
        private const val TAG_PHOTO_URL = "Картинка"
    }

    fun parse(inputStream: InputStream): List<Goods> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.next()
            return readCommerceInfo(parser)
        }
    }

    private fun readCommerceInfo(parser: XmlPullParser): List<Goods> {

        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_COMMERCE_INFO)
        val result = mutableListOf<Goods>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == TAG_CATALOG) {
                result += readCatalog(parser)
            } else {
                skip(parser)
            }
        }
        return result
    }

    private fun readCatalog(parser: XmlPullParser): List<Goods> {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_CATALOG)
        val result = mutableListOf<Goods>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_GOODS_MULTITUDE) {
                result += readGoodsMultitude(parser)
            } else {
                skip(parser)
            }
        }
        return result
    }

    private fun readGoodsMultitude(parser: XmlPullParser): List<Goods> {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_GOODS_MULTITUDE)
        val result = mutableListOf<Goods>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_GOODS) {
                result += readGoods(parser)
            } else {
                skip(parser)
            }
        }
        return result
    }

    private fun readGoods(parser: XmlPullParser): Goods {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_GOODS)

        var id = ""
        var name = ""
        var photoUrl = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                TAG_ID -> id = readId(parser)
                TAG_NAME -> name = readName(parser)
                TAG_PHOTO_URL -> photoUrl = readPhotoUrl(parser)
                else -> skip(parser)
            }
        }
        return Goods(id, name, photoUrl)
    }

    private fun readPhotoUrl(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_PHOTO_URL)
        val url = readText(parser)
        parser.require(XmlPullParser.END_TAG, xmlNamespace, TAG_PHOTO_URL)
        return url
    }

    private fun readName(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_NAME)
        val name = readText(parser)
        parser.require(XmlPullParser.END_TAG, xmlNamespace, TAG_NAME)
        return name
    }

    private fun readId(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_ID)
        val id = readText(parser)
        parser.require(XmlPullParser.END_TAG, xmlNamespace, TAG_ID)
        return id
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException("Syntax error XML")
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}