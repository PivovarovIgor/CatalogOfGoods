package ru.brauer.catalogofgoods.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Goods(
    val id: String,
    val name: String,
    val listOfPhotosUri: List<String>,
    val offers: List<Offer>
) : Parcelable {

    constructor(name: String, photoUrl: String) : this("", name, listOf(photoUrl), listOf())

    companion object {
        fun empty() = Goods(
            id = "",
            name = "",
            listOfPhotosUri = listOf(),
            offers = listOf()
        )
    }
}

@Parcelize
data class Offer(
    val id: String,
    val name: String,
    val stock: Int,
    val price: Price
) : Parcelable

@Parcelize
data class Price(
    val presentation: String,
    val priceValue: Int,
    val currency: String
) : Parcelable {
    companion object {
        fun empty() = Price(
            presentation = "",
            priceValue = 0,
            currency = ""
        )
    }
}