package ru.brauer.catalogofgoods.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GoodsDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(goods: GoodsEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg goods: GoodsEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(goods: List<GoodsEnt>)

    @Query("SELECT * FROM goods")
    fun getAll(): List<GoodsEnt>
}