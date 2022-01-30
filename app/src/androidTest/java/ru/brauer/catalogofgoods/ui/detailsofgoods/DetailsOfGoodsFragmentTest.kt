package ru.brauer.catalogofgoods.ui.detailsofgoods

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods

@RunWith(AndroidJUnit4::class)
class DetailsOfGoodsFragmentTest {

    private lateinit var scenario: FragmentScenario<DetailsOfGoodsFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer()
    }

    @Test
    fun test_showing_catalog_of_goods() {

        val argsOfFragment = bundleOf("DetailsOfGoodsFragment_KEY_USER" to
            Goods(
                id = GOODS_ID,
                name = GOODS_NAME,
                listOfPhotosUri = listOf(),
                offers = listOf(),
                maxPricePresent = GOODS_MAX_PRICE,
                stock = GOODS_STOCK
            )
        )
        val scenario = launchFragmentInContainer<DetailsOfGoodsFragment>(argsOfFragment)

        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.goods_name)).check(matches(withText(GOODS_NAME)))
    }

    companion object {
        private const val GOODS_ID = "some ID of goods"
        private const val GOODS_NAME = "some fragment"
        private const val GOODS_MAX_PRICE = "some price"
        private const val GOODS_STOCK = 1
    }
}