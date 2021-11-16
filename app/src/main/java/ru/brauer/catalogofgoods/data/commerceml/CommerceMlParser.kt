package ru.brauer.catalogofgoods.data.commerceml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import ru.brauer.catalogofgoods.BuildConfig
import ru.brauer.catalogofgoods.data.entities.Goods
import java.io.InputStream

class CommerceMlParser : IXmlParserByRule {

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

    override fun parse(inputStream: InputStream): List<Goods> {
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
            when (parser.name) {
                TAG_ID -> {
                    val id = readPlaintText(parser, TAG_ID)
                    if (id != BuildConfig.ID_GOODS) {
                        stepOutFromCurrentElement(parser)
                    }
                }
                TAG_GOODS_MULTITUDE -> result += readGoodsMultitude(parser)
                else -> skip(parser)
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
                TAG_ID -> id = readPlaintText(parser, TAG_ID)
                TAG_NAME -> name = readPlaintText(parser, TAG_NAME)
                TAG_PHOTO_URL -> photoUrl = readPlaintText(parser, TAG_PHOTO_URL)
                else -> skip(parser)
            }
        }
        return Goods(id, name, photoUrl)
    }

    private fun readPlaintText(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, tag)
        val text = readText(parser)
        parser.require(XmlPullParser.END_TAG, xmlNamespace, tag)
        return text
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

    private fun stepOutFromCurrentElement(parser: XmlPullParser) {
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}