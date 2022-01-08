package ru.brauer.catalogofgoods.data.database


import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import ru.brauer.catalogofgoods.data.database.entities.*

@Database(
    entities = [
        GoodsEnt::class,
        PhotoOfGoodsEnt::class,
        OfferEnt::class,
        PriceEnt::class,
        RestEnt::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract val goodsDao: GoodsDao
    abstract val photoOfGoodsDao: PhotoOfGoodsDao
    abstract val offerDao: OfferDao
    abstract val priceDao: PriceDao
    abstract val restDao: RestDao

    private var dataTimeStartCaching: Long = 0

    fun startUpdatingData() {
        dataTimeStartCaching = TimestampProvider.current()
    }

    fun endUpdatingData() {
        dataTimeStartCaching = 0
    }

    fun deleteNotUpdatedData() {

        if (dataTimeStartCaching > 0) {
            goodsDao.deletePreviouslyUpdatedDates(dataTimeStartCaching)
            offerDao.deletePreviouslyUpdatedDates(dataTimeStartCaching)
            priceDao.deletePreviouslyUpdatedDates(dataTimeStartCaching)
            restDao.deletePreviouslyUpdatedDates(dataTimeStartCaching)
        }
        endUpdatingData()
    }
}