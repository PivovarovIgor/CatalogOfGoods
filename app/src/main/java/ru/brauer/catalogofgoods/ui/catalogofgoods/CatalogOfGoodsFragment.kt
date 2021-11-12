package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
import ru.brauer.catalogofgoods.domain.AppState
import javax.inject.Inject

class CatalogOfGoodsFragment : Fragment() {

    companion object {
        fun newInstance() = CatalogOfGoodsFragment()
    }

    private var binding: FragmentCatalogOfGoodsBinding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
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
    ): View = FragmentCatalogOfGoodsBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.instance.appComponent.inject(this)
        binding?.run {
            listOfGoods.layoutManager =
                GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            listOfGoods.adapter = CatalogOfGoodsAdapter(viewModel, viewLifecycleOwner)
        }
        viewModel.observe(viewLifecycleOwner, ::renderData)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}