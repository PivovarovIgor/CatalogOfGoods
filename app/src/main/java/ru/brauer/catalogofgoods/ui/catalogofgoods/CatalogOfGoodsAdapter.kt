package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.brauer.catalogofgoods.R
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.databinding.ItemGoodsBinding
import ru.brauer.catalogofgoods.domain.AppState

class CatalogOfGoodsAdapter(
    private val viewModel: CatalogOfGoodsViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val itemOpener: (data: Goods) -> Unit
) :
    RecyclerView.Adapter<CatalogOfGoodsAdapter.ViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        viewModel.observe(lifecycleOwner, ::updateShow)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateShow(appState: AppState) {
        if (appState !is AppState.Error) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = viewModel.getItemCount()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogOfGoodsAdapter.ViewHolder =
        ItemGoodsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
            .apply {
                itemView.setOnClickListener { _ ->
                    viewModel.getDataAtPosition(absoluteAdapterPosition)
                        ?.let { itemOpener(it) }
                }
            }

    override fun onBindViewHolder(holder: CatalogOfGoodsAdapter.ViewHolder, position: Int) =
        holder.bindData(position)


    inner class ViewHolder(private val binding: ItemGoodsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            viewModel.getDataAtPosition(position)
                ?.let {
                    binding.goodsName.text = it.name
                    Glide.with(binding.goodsImage)
                        .load(it.photoUrl)
                        .placeholder(R.drawable.ic_baseline_image_24)
                        .error(R.drawable.ic_baseline_broken_image_24)
                        .into(binding.goodsImage)
                }
        }
    }
}