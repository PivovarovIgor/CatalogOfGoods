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

    @Query(
        """
SELECT * FROM offers AS offers
JOIN  rests AS rests
ON offers.goods_id || '#' || offers.id = rests.offer_id
WHERE 
offers.goods_id = :goodsId
AND rests.count <> 0

UNION ALL

SELECT * FROM offers AS offers
JOIN  rests AS rests
ON offers.id = rests.offer_id
WHERE 
offers.goods_id = :goodsId
AND rests.count <> 0
"""
    )
    fun getOffersAndRestsByGoodsId(goodsId: String): Map<OfferEnt, List<RestEnt>>

    @Query(
        """
SELECT * FROM offers AS offers
JOIN  prices AS prices
ON offers.goods_id || '#' || offers.id = prices.offer_id
WHERE 
offers.goods_id = :goodsId
AND prices.type_price_id = :idOfMainPriceType

UNION ALL

SELECT * FROM offers AS offers
JOIN  prices AS prices
ON offers.id = prices.offer_id
WHERE 
offers.goods_id = :goodsId
AND prices.type_price_id = :idOfMainPriceType
"""
    )
    fun getOffersAndPricesByGoodsId(goodsId: String, idOfMainPriceType: String): Map<OfferEnt, List<PriceEnt>>
}