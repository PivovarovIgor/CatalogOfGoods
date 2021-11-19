package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = GoodsEnt::class,
        parentColumns = ["id"],
        childColumns = ["goods_id"],
        onDelete = ForeignKey.NO_ACTION
    )], tableName = "offers"
)
data class OfferEnt(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "goods_id") val goodsId: String
)