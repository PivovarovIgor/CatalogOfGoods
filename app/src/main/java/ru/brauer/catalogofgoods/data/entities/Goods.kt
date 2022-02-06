package ru.brauer.catalogofgoods.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Goods(
    val id: String,
    val name: CharSequence,
    val listOfPhotosUri: List<String>,
    val offers: List<Offer>,
    val maxPricePresent: String,
    val stock: Int
) : Parcelable {

    constructor(name: String, photoUrl: String) : this(
        "",
        name,
        listOf(photoUrl),
        listOf(),
        EMPTY_PRICE,
        0
    )

    companion object {
        const val EMPTY_PRICE = "---"
        fun empty() = Goods(
            id = "",
            name = "",
            listOfPhotosUri = listOf(),
            offers = listOf(),
            maxPricePresent = EMPTY_PRICE,
            stock = 0
        )
    }
}

@Parcelize
data class Offer(
    val id: String,
    val name: String,
    val stock: Int,
    val price: Price,
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