package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rests: RestEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg rests: RestEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rests: List<RestEnt>)

    @Query("SELECT * FROM rests")
    fun getAll(): List<RestEnt>

    @Query("DELETE FROM rests WHERE data_time_updated < :dataTime")
    fun deletePreviouslyUpdatedDates(dataTime: Long)

    @Query("SELECT * FROM rests WHERE offer_id IN (:offersId)")
    fun getRestsByOffersId(offersId: List<String>): List<RestEnt>
}