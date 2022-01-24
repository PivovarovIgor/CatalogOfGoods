package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.ui.IScreens
import javax.inject.Inject

class CatalogOfGoodsFragment : Fragment() {

    companion object {
        fun newInstance() = CatalogOfGoodsFragment()
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
        CatalogOfGoodsAdapter(goodsComparator) {
            router.navigateTo(screens.detailsOfGoods(it))
        }
    }

    private val viewModel: CatalogOfGoodsViewModel by lazy {
        ViewModelProvider(
            this@CatalogOfGoodsFragment,
            viewModelFactory
        ).get(CatalogOfGoodsViewModel::class.java)
    }

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
        initRecyclerView()
        viewModel.observe(viewLifecycleOwner, ::renderData, ::renderBackGroundProcess)
        Handler(Looper.myLooper()!!).postDelayed(
            {
                viewModel.setFilterOnStock = true
                Toast.makeText(context, "applied the filter on in stock", Toast.LENGTH_LONG).show()
                pagingAdapter.refresh()
            },
            10_000L
        )
    }

    private fun initRecyclerView() {

        App.instance.appComponent.inject(this)
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
}