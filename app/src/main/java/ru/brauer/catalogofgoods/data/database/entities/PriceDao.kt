package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(prices: PriceEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg prices: PriceEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(prices: List<PriceEnt>)

    @Query("DELETE FROM prices WHERE data_time_updated < :dataTime")
    fun deletePreviouslyUpdatedDates(dataTime: Long)

    @Query("SELECT * FROM prices WHERE offer_id LIKE :goodsId AND type_price_id = :idOfMainPriceType")
    fun getPricesByOffersId(goodsId: String, idOfMainPriceType: String): List<PriceEnt>
}