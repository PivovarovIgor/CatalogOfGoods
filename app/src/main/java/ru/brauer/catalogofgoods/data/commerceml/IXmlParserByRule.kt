package ru.brauer.catalogofgoods.data.commerceml

import io.reactivex.rxjava3.core.Observable
import java.io.InputStream

interface IXmlParserByRule {
    fun parse(inputStream: InputStream, fileName: String): Observable<List<EntityOfCommerceMl>>
}