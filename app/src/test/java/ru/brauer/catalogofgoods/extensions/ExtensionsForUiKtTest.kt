package ru.brauer.catalogofgoods.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsForUiKtTest {

    private val someString = "О том, что подарит нам завтра" +
            "Уже не помню мотив" +
            "Осколками строки от сердца"

    @Test
    fun get_all_contains_found_case() {
        assertEquals(
            someString.getAllContains("То"),
            listOf(2 to 4, 8 to 10)
        )
    }

    @Test
    fun get_all_contains_not_found_case() {
        assertEquals(
            someString.getAllContains("Нет этого"),
            listOf<Pair<Int, Int>>()
        )
    }

    @Test
    fun get_all_contains_in_empty_string_case() {
        assertEquals(
            "".getAllContains("Нет этого"),
            listOf<Pair<Int, Int>>()
        )
    }

    @Test
    fun get_all_contains_by_empty_substring_case() {
        assertEquals(
            someString.getAllContains(""),
            listOf<Pair<Int, Int>>()
        )
    }
}