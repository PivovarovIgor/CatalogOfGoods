package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
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
                    stopShimmerIfItStarted()
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
                } ?: startShimmer()
        }
        private fun startShimmer() {
            with(binding) {
                cardOfGoods.visibility = View.GONE
                cardOfGoodsShimmer.visibility = View.VISIBLE
                cardOfGoodsShimmer.startShimmer()
            }
        }
        private fun stopShimmerIfItStarted() {
            with(binding) {
                if (cardOfGoods.visibility == View.GONE) {
                    cardOfGoodsShimmer.stopShimmer()
                    cardOfGoodsShimmer.visibility = View.GONE
                    cardOfGoods.visibility = View.VISIBLE
                }
            }
        }
    }
}