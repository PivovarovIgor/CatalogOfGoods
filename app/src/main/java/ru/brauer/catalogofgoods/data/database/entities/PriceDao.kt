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

    @Query("SELECT * FROM prices")
    fun getAll(): List<PriceEnt>
}