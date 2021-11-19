package ru.brauer.catalogofgoods.data.commerceml

sealed class EntityOfCommerceMl {

    data class Goods(
        val id: String,
        val name: String,
        val photoUrl: List<String>
    ) : EntityOfCommerceMl()

    data class Offer(
        val id: String,
        val name: String
    ) : EntityOfCommerceMl()
}