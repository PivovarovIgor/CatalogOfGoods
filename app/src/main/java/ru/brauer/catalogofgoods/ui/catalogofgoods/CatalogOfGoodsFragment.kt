package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.services.LoadingGoodsService
import ru.brauer.catalogofgoods.ui.IScreens
import javax.inject.Inject

class CatalogOfGoodsFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = CatalogOfGoodsFragment()
        private const val NEW_SEARCH_QUERY_DELAY = 2000L
    }

    private var binding: FragmentCatalogOfGoodsBinding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var screens: IScreens

    private val goodsComparator = object : DiffUtil.ItemCallback<Goods>() {
        override fun areItemsTheSame(oldItem: Goods, newItem: Goods): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Goods, newItem: Goods): Boolean =
            oldItem == newItem
    }

    private val pagingAdapter: CatalogOfGoodsAdapter by lazy {
        CatalogOfGoodsAdapter(goodsComparator, lifecycleScope) {
            router.navigateTo(screens.detailsOfGoods(it))
        }
    }

    private val viewModel: CatalogOfGoodsViewModel by viewModels { viewModelFactory }

    private var searchQueryText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCatalogOfGoodsBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.instance.appComponent.inject(this)
        initRecyclerView()
        searchQueryText = viewModel.searchQueryText
        viewModel.observe(viewLifecycleOwner, ::renderData, ::renderBackGroundProcess)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    private fun initRecyclerView() {
        binding?.run {
            listOfGoods.layoutManager =
                GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            listOfGoods.adapter = pagingAdapter

            lifecycleScope.launch {
                viewModel.dataPagingFlow.collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun renderData(appState: AppState) {
        binding?.run {
            progressBar.visibility =
                if (appState is AppState.Loading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        if (appState is AppState.Error) {
            context?.run {
                alertDialog(appState.exception.message, R.string.app_state_error)
            }
        } else if (appState is AppState.upDateOnSearch) {
            if (appState.query != searchQueryText) {
                searchQueryText = appState.query
                pagingAdapter.refresh()
            }
        }
    }

    private fun Context.alertDialog(message: String?, @StringRes idRes: Int) =
        AlertDialog.Builder(this)
            .setTitle(idRes)
            .setMessage(message)
            .show()

    private fun renderBackGroundProcess(backgroundLoadingState: BackgroundLoadingState) {
        binding?.run {
            backgroundLoadingProcessGroup.visibility =
                if (backgroundLoadingState is BackgroundLoadingState.LoadingState) View.VISIBLE else View.GONE
            when (backgroundLoadingState) {
                is BackgroundLoadingState.LoadingState -> {
                    titleBackgroundProcessing.text =
                        getString(R.string.background_loading, backgroundLoadingState.count)
                }
                is BackgroundLoadingState.Error -> {
                    context?.run {
                        alertDialog(
                            backgroundLoadingState.exception.message,
                            R.string.app_backgroung_error
                        )
                    }
                }
                is BackgroundLoadingState.Complete -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.options_menu_of_goods_catalog, menu)
        val searchMenuItem = menu.findItem(R.id.action_search_goods)
        val searchView = searchMenuItem?.actionView as? SearchView

        if (searchQueryText.isNotBlank()) {
            searchView?.let {
                searchMenuItem.expandActionView()
                it.setQuery(searchQueryText, false)
                it.clearFocus()
            }
        }

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            var job: Job? = null

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                job?.cancel()
                job = lifecycleScope.launch {
                    if (newText?.isNotBlank() == true) {
                        delay(NEW_SEARCH_QUERY_DELAY)
                    }
                    viewModel.onSearchQueryChanged(newText ?: "")
                }
                return true
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {
            R.id.action_load_data -> {
                Intent(context, LoadingGoodsService::class.java)
                    .also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context?.startForegroundService(it)
                        }

                    }
                true
            }
            else -> false
        }
}