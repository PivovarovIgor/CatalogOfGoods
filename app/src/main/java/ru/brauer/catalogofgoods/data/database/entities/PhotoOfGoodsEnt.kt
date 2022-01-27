package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "photos_of_goods",
    primaryKeys = [
        "goods_id",
        "photo_url"
    ],
    foreignKeys = [
        ForeignKey(
            entity = GoodsEnt::class,
            parentColumns = ["id"],
            childColumns = ["goods_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PhotoOfGoodsEnt(
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "goods_id") val goodsId: String,
)