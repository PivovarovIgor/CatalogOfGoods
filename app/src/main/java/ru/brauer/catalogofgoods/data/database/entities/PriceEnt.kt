package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "prices",
    primaryKeys = [
        "offer_id",
        "type_price_id"
    ]
)
data class PriceEnt(
    @ColumnInfo(name = "offer_id", index = true) val offerId: String,
    val presentation: String,
    @ColumnInfo(name = "type_price_id") val typePriceId: String,
    @ColumnInfo(name = "price_value") val priceValue: Int,
    val currency: String,
    @ColumnInfo(name = "data_time_updated") val dataTimeUpdated: Long = TimestampProvider.current()
)