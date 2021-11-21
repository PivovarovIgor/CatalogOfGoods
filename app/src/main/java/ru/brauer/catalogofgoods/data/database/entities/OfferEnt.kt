package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    indices = [
        Index("goods_id", name = "offers_for_goods_idx")
    ],
    tableName = "offers"
)
data class OfferEnt(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "goods_id") val goodsId: String
)