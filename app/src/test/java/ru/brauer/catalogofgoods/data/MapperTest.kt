package ru.brauer.catalogofgoods.data

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt

class MapperTest {

    @Test
    fun map_CommerceMLGoods_to_database_success() {

        val initialData = EntityOfCommerceMl.Goods(
            id = "some id",
            name = "some name",
            photoUrl = listOf("photo1")
        )

        val targetResult = GoodsEnt(
            id = "some id",
            name = "some name",
            photoUrl = "photo1"
        )

        assertEquals(initialData.toDatabaseData(), targetResult)
    }
}