package ru.brauer.catalogofgoods.data.commerceml

import io.reactivex.rxjava3.core.Observable
import ru.brauer.catalogofgoods.data.entities.Goods
import java.io.InputStream

interface IXmlParserByRule {
    fun parse(inputStream: InputStream): Observable<List<Goods>>
}