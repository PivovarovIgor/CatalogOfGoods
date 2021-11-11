package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding
import ru.brauer.catalogofgoods.di.viewmodel.ViewModelFactory
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
        val name = viewModel.name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}