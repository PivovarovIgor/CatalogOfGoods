package ru.brauer.catalogofgoods.data.commerceml

import ru.brauer.catalogofgoods.data.entities.Goods
import java.io.InputStream

interface IXmlParserByRule {
    fun parse(inputStream: InputStream): List<Goods>
}