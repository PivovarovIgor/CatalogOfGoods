package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "goods")
data class GoodsEnt(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name", index = true) val name: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "data_time_updated") val dataTimeUpdated: Long = TimestampProvider.current()
)