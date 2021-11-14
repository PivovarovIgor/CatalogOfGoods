package ru.brauer.catalogofgoods.data.commerceml

import ru.brauer.catalogofgoods.data.Goods
import java.io.InputStream

interface IXmlParserByRule {
    fun parse(inputStream: InputStream): List<Goods>
}