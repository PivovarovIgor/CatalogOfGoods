package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.ItemGoodsBinding
import ru.brauer.catalogofgoods.extensions.loadFirstImage
import java.math.BigDecimal

class CatalogOfGoodsAdapter(
    diffCallback: DiffUtil.ItemCallback<Goods>,
    private val itemOpener: (data: Goods) -> Unit
) : PagingDataAdapter<Goods, CatalogOfGoodsAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogOfGoodsAdapter.ViewHolder =
        ItemGoodsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
            .apply {
                itemView.setOnClickListener { _ ->
                    getItem(absoluteAdapterPosition)
                        ?.let { itemOpener(it) }
                }
            }

    override fun onBindViewHolder(holder: CatalogOfGoodsAdapter.ViewHolder, position: Int) =
        holder.bindData(position)


    inner class ViewHolder(private val binding: ItemGoodsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            getItem(position)
                ?.let { goods ->
                    binding.goodsName.text = goods.name
                    binding.goodsImage.loadFirstImage(goods.listOfPhotosUri)
                    binding.price.text = "Price: ${
                        goods.offers.maxOfOrNull {
                            it.price.priceValue.toBigDecimal().divide(
                                BigDecimal.valueOf(100)
                            )
                        } ?: "---"
                    } In stock: ${goods.offers.sumOf { it.stock }}"
                } ?: let {
                binding.goodsName.text = "---"
                binding.goodsImage.background = null
            }
        }
    }
}