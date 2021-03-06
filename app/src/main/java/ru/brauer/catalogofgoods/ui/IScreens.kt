package ru.brauer.catalogofgoods.ui

import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.androidx.FragmentScreen
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.ui.catalogofgoods.CatalogOfGoodsFragment
import ru.brauer.catalogofgoods.ui.detailsofgoods.DetailsOfGoodsFragment

interface IScreens {
    fun catalogOfGoods(): Screen
    fun detailsOfGoods(goods: Goods): Screen
}

class AndroidScreens : IScreens {
    override fun catalogOfGoods(): Screen = FragmentScreen { CatalogOfGoodsFragment.newInstance() }
    override fun detailsOfGoods(goods: Goods): Screen =
        FragmentScreen { DetailsOfGoodsFragment.newInstance(goods) }
}