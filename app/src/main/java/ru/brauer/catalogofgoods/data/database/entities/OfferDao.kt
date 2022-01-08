package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(offers: OfferEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg offers: OfferEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(offers: List<OfferEnt>)

    @Query("DELETE FROM offers WHERE data_time_updated < :dataTime")
    fun deletePreviouslyUpdatedDates(dataTime: Long)

    @Query("SELECT * FROM offers WHERE goods_id = :goodsId")
    fun getOffersByGoodsId(goodsId: String): List<OfferEnt>
}