package ru.brauer.catalogofgoods.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import dagger.hilt.android.AndroidEntryPoint
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.databinding.ActivityMainBinding
import ru.brauer.catalogofgoods.ui.IScreens
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navigation = AppNavigator(this, R.id.container)

    @Inject lateinit var navigationHolder: NavigatorHolder
    @Inject lateinit var router: Router
    @Inject lateinit var screens: IScreens

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        router.replaceScreen(screens.catalogOfGoods())
    }

    override fun onResume() {
        super.onResume()
        navigationHolder.setNavigator(navigation)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }
}