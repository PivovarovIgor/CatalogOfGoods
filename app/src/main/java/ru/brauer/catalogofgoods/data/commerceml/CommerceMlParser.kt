package ru.brauer.catalogofgoods.data.commerceml

import android.util.Xml
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import ru.brauer.catalogofgoods.BuildConfig
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException

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
        private const val TAG_PACKAGE_OF_OFFERS = "ПакетПредложений"
        private const val TAG_OFFERS = "Предложения"
        private const val TAG_OFFER = "Предложение"
    }

    private lateinit var commerceMlEmitter: CommerceInfoEmitter

    override fun parse(inputStream: InputStream): Observable<List<EntityOfCommerceMl>> =
        Observable.create { emitter ->
            commerceMlEmitter = CommerceInfoEmitter(emitter)
            try {
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)
                parser.next()
                readCommerceInfo(parser)
                commerceMlEmitter.complete()
            } catch (exception: IOException) {
                commerceMlEmitter.error(exception)
            }
        }

    private fun readCommerceInfo(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_COMMERCE_INFO)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                TAG_CATALOG -> readCatalog(parser)
                TAG_PACKAGE_OF_OFFERS -> readPackageOfOffers(parser)
                else -> skip(parser)
            }
        }
    }

    private fun readPackageOfOffers(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_PACKAGE_OF_OFFERS)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == TAG_OFFERS) {
                readOffers(parser)
            } else {
                skip(parser)
            }
        }
    }

    private fun readOffers(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_OFFERS)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == TAG_OFFER) {
                commerceMlEmitter.next(readOffer(parser))
            } else {
                skip(parser)
            }
        }
    }

    private fun readOffer(parser: XmlPullParser): EntityOfCommerceMl {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_OFFER)

        var id: String = ""
        var name: String = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                TAG_ID -> id = readPlaintText(parser, TAG_ID)
                TAG_NAME -> name = readPlaintText(parser, TAG_NAME)
                else -> skip(parser)
            }
        }
        return EntityOfCommerceMl.Offer(id, name)
    }

    private fun readCatalog(parser: XmlPullParser) {
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
                TAG_GOODS_MULTITUDE -> readGoodsMultitude(parser)
                else -> skip(parser)
            }
        }
    }

    private fun readGoodsMultitude(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_GOODS_MULTITUDE)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == TAG_GOODS) {
                commerceMlEmitter.next(readGoods(parser))
            } else {
                skip(parser)
            }
        }
    }

    private fun readGoods(parser: XmlPullParser): EntityOfCommerceMl.Goods {
        parser.require(XmlPullParser.START_TAG, xmlNamespace, TAG_GOODS)

        var id = ""
        var name = ""
        val photoUrl = mutableListOf<String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                TAG_ID -> id = readPlaintText(parser, TAG_ID)
                TAG_NAME -> name = readPlaintText(parser, TAG_NAME)
                TAG_PHOTO_URL -> photoUrl += readPlaintText(parser, TAG_PHOTO_URL)
                else -> skip(parser)
            }
        }
        return EntityOfCommerceMl.Goods(id, name, photoUrl)
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

    private class CommerceInfoEmitter(private val emitter: ObservableEmitter<List<EntityOfCommerceMl>>) {

        companion object {
            private const val CHUNK_OF_DATA = 10
        }

        private var entitiesOfCommerceMl = mutableListOf<EntityOfCommerceMl>()

        fun next(entityOfCommerceMl: EntityOfCommerceMl) {
            entitiesOfCommerceMl += entityOfCommerceMl
            if (entitiesOfCommerceMl.count() == CHUNK_OF_DATA) {
                push()
            }
        }

        fun complete() {
            push()
            emitter.onComplete()
        }

        fun error(exception: Throwable) {
            //push()
            emitter.onError(exception)
        }

        private fun push() {
            if (entitiesOfCommerceMl.isNotEmpty()) {
                emitter.onNext(entitiesOfCommerceMl)
                entitiesOfCommerceMl = mutableListOf()
            }
        }
    }
}

