package ru.brauer.catalogofgoods.ui.detailsofgoods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.data.entities.Goods
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
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> throw IllegalStateException("The view type $viewType is not defined")
        }
    }

    override fun onBindViewHolder(holder: DetailsOfGoodsAdapter.BaseViewHolder, position: Int) =
        holder.bind(position)

    override fun getItemCount(): Int = 1

    override fun getItemViewType(position: Int): Int = VIEW_TYPE_PHOTO_OF_GOODS

    inner class PhotoOfGoodsViewHolder(private val binding: ItemDetailsOfGoodsPhotoBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            binding.goodsImage.loadFirstImage(goods.listOfPhotosUri)
        }
    }

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(position: Int)
    }

    companion object {
        private const val VIEW_TYPE_PHOTO_OF_GOODS = 0
    }
}