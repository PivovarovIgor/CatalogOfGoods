package ru.brauer.catalogofgoods.data.database.entities

object TimestampProvider {
    fun current(): Long = System.currentTimeMillis()
}