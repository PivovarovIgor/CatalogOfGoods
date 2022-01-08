package ru.brauer.catalogofgoods.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt
import ru.brauer.catalogofgoods.extensions.convertPathOfPhotoRelativelyFileName

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

    @Test
    fun map_CommerceMLGoods_converting_path_of_photo_success() {

        val fileName = "goods/1/import___95da7a1c-ed15-46a9-91e2-f66bdfc270ec.xml"
        val initialData = listOf(
            EntityOfCommerceMl.Goods(
                id = "id1",
                name = "name1",
                photoUrl = listOf(
                    "import_files/06/photo1.jpg",
                    "import_files/06/photo2.jpg",
                    "import_files/06/photo3.jpg"
                )
            ),
            EntityOfCommerceMl.Goods(
                id = "id2",
                name = "name2",
                photoUrl = listOf(
                    "import_files/08/ph1.jpg",
                    "import_files/08/ph2.jpg",
                    "import_files/08/ph3.jpg"
                )
            )
        )
        val targetResult = listOf(
            EntityOfCommerceMl.Goods(
                id = "id1",
                name = "name1",
                photoUrl = listOf(
                    "goods/1/import_files/06/photo1.jpg",
                    "goods/1/import_files/06/photo2.jpg",
                    "goods/1/import_files/06/photo3.jpg"
                )
            ),
            EntityOfCommerceMl.Goods(
                id = "id2",
                name = "name2",
                photoUrl = listOf(
                    "goods/1/import_files/08/ph1.jpg",
                    "goods/1/import_files/08/ph2.jpg",
                    "goods/1/import_files/08/ph3.jpg"
                )
            )
        )

        assertArrayEquals(
            initialData.convertPathOfPhotoRelativelyFileName(fileName).toTypedArray(),
            targetResult.toTypedArray()
        )
    }
}