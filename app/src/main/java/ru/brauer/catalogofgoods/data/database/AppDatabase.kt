package ru.brauer.catalogofgoods.data.database


import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ru.brauer.catalogofgoods.data.database.entities.GoodsDao
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt
import ru.brauer.catalogofgoods.data.database.entities.OfferDao
import ru.brauer.catalogofgoods.data.database.entities.OfferEnt

@Database(
    entities = [
        GoodsEnt::class,
        OfferEnt::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract val goodsDao: GoodsDao
    abstract val offerDao: OfferDao
}