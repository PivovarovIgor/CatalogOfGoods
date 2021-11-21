package ru.brauer.catalogofgoods.data.commerceml

sealed class EntityOfCommerceMl {

    data class Goods(
        val id: String,
        val name: String,
        val photoUrl: List<String>
    ) : EntityOfCommerceMl()

    data class Offer(
        val id: String,
        val name: String,
        val prices: List<Price>,
        val rests: List<Rest>
    ) : EntityOfCommerceMl()
}

data class Price(
    val name: String,
    val typePriceId: String,
    val price: String,
    val currency: String
)

data class Rest(
    val count: String
)