package ru.brauer.catalogofgoods.data.database.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoOfGoodsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photoOfGoods: PhotoOfGoodsEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg photoOfGoods: PhotoOfGoodsEnt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photoOfGoods: List<PhotoOfGoodsEnt>)

    @Query("SELECT * FROM photos_of_goods")
    fun getAll(): List<PhotoOfGoodsEnt>
}