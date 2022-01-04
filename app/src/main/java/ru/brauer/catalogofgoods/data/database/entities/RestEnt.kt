package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rests")
data class RestEnt(
    @PrimaryKey @ColumnInfo(name = "offer_id") val offerId: String,
    val count: Int,
    @ColumnInfo(name = "data_time_updated") val dataTimeUpdated: Long = TimestampProvider.current()
)