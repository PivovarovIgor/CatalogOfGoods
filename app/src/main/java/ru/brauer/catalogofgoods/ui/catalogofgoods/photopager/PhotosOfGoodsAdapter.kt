package ru.brauer.catalogofgoods.ui.catalogofgoods.photopager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.brauer.catalogofgoods.databinding.ItemPhotoBinding
import ru.brauer.catalogofgoods.extensions.loadFirstImage

class PhotosOfGoodsAdapter : RecyclerView.Adapter<PhotosOfGoodsAdapter.PhotoOfGoodsViewHolder>() {

    var photos: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoOfGoodsViewHolder =
        ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::PhotoOfGoodsViewHolder)

    override fun onBindViewHolder(holder: PhotoOfGoodsViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.count()

    inner class PhotoOfGoodsViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: String) {
            binding.goodsImage.loadFirstImage(photo)
        }
    }
}