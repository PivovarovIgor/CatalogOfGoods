package ru.brauer.catalogofgoods.data.commerceml

import android.util.Xml
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
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

        private const val CHUNK_OF_DATA = 10
    }

    override fun parse(inputStream: InputStream): Observable<List<Goods>> =
        Observable.create { emitter ->
            inputStream.use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)
                parser.next()
                readCommerceInfo(parser, emitter)
                emitter.onComplete()
            }
        }

    private fun readCommerceInfo(
        parser: XmlPullParser,
        emitter: ObservableEmitter<List<Goods>>
    ) {

        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_COMMERCE_INFO)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == TAG_CATALOG) {
                readCatalog(parser, emitter)
            } else {
                skip(parser)
            }
        }
    }

    private fun readCatalog(
        parser: XmlPullParser,
        emitter: ObservableEmitter<List<Goods>>
    ) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_CATALOG)
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
                TAG_GOODS_MULTITUDE -> readGoodsMultitude(parser, emitter)
                else -> skip(parser)
            }
        }
    }

    private fun readGoodsMultitude(
        parser: XmlPullParser,
        emitter: ObservableEmitter<List<Goods>>
    ) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_GOODS_MULTITUDE)
        var result = mutableListOf<Goods>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_GOODS) {
                result += readGoods(parser)
                if (result.count() >= CHUNK_OF_DATA) {
                    emitter.onNext(result)
                    result = mutableListOf()
                }
            } else {
                skip(parser)
            }
        }
        if (result.count() > 0) {
            emitter.onNext(result)
        }
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