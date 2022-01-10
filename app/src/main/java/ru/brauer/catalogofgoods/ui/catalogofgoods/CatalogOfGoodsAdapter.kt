package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.ItemGoodsBinding
import ru.brauer.catalogofgoods.extensions.loadFirstImage

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

        fun bindData(position: Int) = with(binding) {
            getItem(position)
                ?.let { goods ->
                    goodsName.text = goods.name
                    goodsImage.loadFirstImage(goods.listOfPhotosUri)
                    price.text = itemView.resources.getString(
                        R.string.price,
                        goods.maxPricePresent,
                    )
                    stock.text = itemView.resources.getString(
                        R.string.stock,
                        goods.stock,
                    )
                } ?: let {
                goodsName.text = "---"
                goodsImage.background = null
            }
        }
    }
}