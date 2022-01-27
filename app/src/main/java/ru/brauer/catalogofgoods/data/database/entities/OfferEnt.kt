package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "offers")
data class OfferEnt(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "goods_id", index = true) val goodsId: String,
    @ColumnInfo(name = "data_time_updated") val dataTimeUpdated: Long = TimestampProvider.current()
) {
    override fun equals(other: Any?): Boolean {
        return id == (other as? OfferEnt)?.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}