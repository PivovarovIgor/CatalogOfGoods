package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.ItemGoodsBinding
import ru.brauer.catalogofgoods.ui.catalogofgoods.photopager.PhotosOfGoodsAdapter

class CatalogOfGoodsAdapter(
    diffCallback: DiffUtil.ItemCallback<Goods>,
    private val itemOpener: (data: Goods) -> Unit
) : PagingDataAdapter<Goods, CatalogOfGoodsAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogOfGoodsAdapter.ViewHolder =
        ItemGoodsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let {
                val adapter = PhotosOfGoodsAdapter()
                ViewHolder(it, adapter)
            }
            .apply {
                itemView.setOnClickListener { _ ->
                    getItem(absoluteAdapterPosition)
                        ?.let { itemOpener(it) }
                }
            }

    override fun onBindViewHolder(holder: CatalogOfGoodsAdapter.ViewHolder, position: Int) =
        holder.bindData(position)


    inner class ViewHolder(
        private val binding: ItemGoodsBinding,
        private val adapter: PhotosOfGoodsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        val snapHelper = PagerSnapHelper()
            .apply {
                attachToRecyclerView(binding.photosOfGoods)
            }

        init {
            binding.photosOfGoods.addItemDecoration(LinePagerIndicatorDecoration())
        }

        fun bindData(position: Int) = with(binding) {
            getItem(position)
                ?.let { goods ->
                    stopShimmerIfItStarted()
                    goodsName.text = goods.name
                    adapter.photos = goods.listOfPhotosUri
                    photosOfGoods.adapter = adapter
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