package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.view.View
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.ui.MainActivity

@RunWith(AndroidJUnit4::class)
class CatalogOfGoodsRecyclerViewTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun activity_search_some_goods_by_name() {

        onView(withId(R.id.search_button)).perform(click())
        onView(withId(R.id.search_src_text)).perform(typeText(SEARCHING_TEXT))

        onView(isRoot()).perform(delay(FIVE_SECONDS))

        onView(withId(R.id.list_of_goods)).check(matches(isDisplayed()))

        scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.list_of_goods)
            assertTrue(recyclerView.isNotEmpty())
        }
    }

    @Test
    fun activity_scroll_to_position() {
        waitDataLoading()
        onView(withId(R.id.list_of_goods))
            .perform(
                RecyclerViewActions.scrollToPosition<CatalogOfGoodsAdapter.ViewHolder>(
                    SOME_POSITION
                )
            )
    }

    @Test
    fun activity_perform_click_position() {
        waitDataLoading()
        onView(withId(R.id.list_of_goods))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<CatalogOfGoodsAdapter.ViewHolder>(
                    0,
                    click()
                )
            )
    }

    @Test
    fun activity_perform_click_on_item() {
        waitDataLoading()
        onView(withId(R.id.list_of_goods))
            .perform(
                RecyclerViewActions.scrollToPosition<CatalogOfGoodsAdapter.ViewHolder>(
                    SOME_POSITION + OFFSET_VISIBILITY_POSITION
                ),
                RecyclerViewActions.actionOnItemAtPosition<CatalogOfGoodsAdapter.ViewHolder>(
                    SOME_POSITION, click()
                )
            )
    }

    private fun waitDataLoading() {
        onView(isRoot()).perform(delay(TWO_SECONDS))
    }

    private fun delay(seconds: Int): ViewAction = object : ViewAction {

        private val MILLISECONDS_OF_ONE_SECOND = 1000L

        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription(): String = "wait $seconds seconds"

        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadForAtLeast(seconds * MILLISECONDS_OF_ONE_SECOND)
        }
    }

    @After
    fun close() {
        scenario.close()
    }

    companion object {
        private const val FIVE_SECONDS = 5
        private const val TWO_SECONDS = 2
        private const val SEARCHING_TEXT = "prof"
        private const val SOME_POSITION = 100
        private const val OFFSET_VISIBILITY_POSITION = 7
    }
}