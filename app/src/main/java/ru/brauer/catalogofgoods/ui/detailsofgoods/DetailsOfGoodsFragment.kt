package ru.brauer.catalogofgoods.ui.detailsofgoods

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.FragmentDetailsOfGoodsBinding
import javax.inject.Inject

class DetailsOfGoodsFragment : Fragment() {

    companion object {

        private const val KEY_USER = "DetailsOfGoodsFragment_KEY_USER"

        fun newInstance(goods: Goods) =
            DetailsOfGoodsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_USER, goods)
                }
            }
    }

    @Inject
    lateinit var factoryViewModel: FactoryDetailsOfGoodsViewModel
    private val viewModel: DetailsOfGoodsViewModel by viewModels {
        factoryViewModel.create(arguments?.getParcelable(KEY_USER) ?: Goods.empty())
    }

    private var binding: FragmentDetailsOfGoodsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        App.instance.appComponent.inject(this)
        return FragmentDetailsOfGoodsBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderData() {
        binding?.run {
            goodsName.text = viewModel.goods.name
            listOfDetails.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            listOfDetails.adapter = DetailsOfGoodsAdapter(viewModel.goods)
            listOfDetails.adapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}

