package ru.brauer.catalogofgoods.data.database


import androidx.room.Database
import androidx.room.RoomDatabase
import ru.brauer.catalogofgoods.data.database.entities.*

@Database(
    entities = [
        GoodsEnt::class,
        OfferEnt::class,
        PriceEnt::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract val goodsDao: GoodsDao
    abstract val offerDao: OfferDao
    abstract val priceDao: PriceDao
}