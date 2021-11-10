package ru.brauer.catalogofgoods.ui

import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.androidx.FragmentScreen
import ru.brauer.catalogofgoods.ui.main.CatalogOfGoodsFragment

interface IScreens {
    fun catalogOfGoods(): Screen
}
class AndroidScreens : IScreens {
    override fun catalogOfGoods(): Screen = FragmentScreen { CatalogOfGoodsFragment.newInstance() }
}