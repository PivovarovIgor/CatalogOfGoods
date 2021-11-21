package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEnt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "offer_id", index = true) val offerId: String,
    val presentation: String,
    @ColumnInfo(name = "type_price_id") val typePriceId: String,
    @ColumnInfo(name = "price_value") val priceValue: Int,
    val currency: String
)