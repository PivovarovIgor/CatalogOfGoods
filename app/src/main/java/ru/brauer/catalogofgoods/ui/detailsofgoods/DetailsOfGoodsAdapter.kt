package ru.brauer.catalogofgoods.ui.detailsofgoods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.ItemDetailsOfGoodsOfferBinding
import ru.brauer.catalogofgoods.databinding.ItemDetailsOfGoodsPhotoBinding
import ru.brauer.catalogofgoods.extensions.loadFirstImage

class DetailsOfGoodsAdapter(
    private val goods: Goods
) : RecyclerView.Adapter<DetailsOfGoodsAdapter.BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailsOfGoodsAdapter.BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_PHOTO_OF_GOODS -> PhotoOfGoodsViewHolder(
                ItemDetailsOfGoodsPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_OFFER_OF_GOODS -> OfferOfGoodsViewHolder(
                ItemDetailsOfGoodsOfferBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("The view type $viewType is not defined")
        }
    }

    override fun onBindViewHolder(holder: DetailsOfGoodsAdapter.BaseViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount(): Int = goods.offers.count() + OFFERS_INDEX_OFFSET

    override fun getItemViewType(position: Int): Int =
        if (position == 0) {
            VIEW_TYPE_PHOTO_OF_GOODS
        } else {
            VIEW_TYPE_OFFER_OF_GOODS
        }

    inner class PhotoOfGoodsViewHolder(private val binding: ItemDetailsOfGoodsPhotoBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            binding.goodsImage.loadFirstImage(goods.listOfPhotosUri)
        }
    }

    inner class OfferOfGoodsViewHolder(private val binding: ItemDetailsOfGoodsOfferBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            val indexOfOffer = position - OFFERS_INDEX_OFFSET
            val offer = goods.offers[indexOfOffer]
            with(binding) {
                offerName.text = offer.name
                price.text = itemView.resources.getString(R.string.price, offer.price.presentation)
                stock.text = itemView.resources.getString(R.string.stock, offer.stock)
            }
        }
    }

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(position: Int)
    }

    companion object {
        private const val VIEW_TYPE_PHOTO_OF_GOODS = 0
        private const val VIEW_TYPE_OFFER_OF_GOODS = 1

        private const val OFFERS_INDEX_OFFSET = 1
    }
}