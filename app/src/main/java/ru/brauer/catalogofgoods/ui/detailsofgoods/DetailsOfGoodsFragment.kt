package ru.brauer.catalogofgoods.ui.detailsofgoods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.FragmentDetailsOfGoodsBinding

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

    private var binding: FragmentDetailsOfGoodsBinding? = null
    private val goods: Goods by lazy {
        arguments?.getParcelable(KEY_USER) ?: Goods.empty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentDetailsOfGoodsBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderData()
    }

    private fun renderData() {
        binding?.run {
            goodsName.text = goods.name
            Glide.with(goodsImage)
                .load(goods.photoUrl)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .into(goodsImage)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}