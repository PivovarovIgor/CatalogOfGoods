package ru.brauer.catalogofgoods.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GoodsEnt::class
    ],
    version = 1
)
abstract class AppDatabase(): RoomDatabase() {
    abstract val goodsDao: GoodsDao
}