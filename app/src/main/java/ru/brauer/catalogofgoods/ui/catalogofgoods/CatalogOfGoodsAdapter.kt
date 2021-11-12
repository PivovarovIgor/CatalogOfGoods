package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.brauer.catalogofgoods.databinding.ItemGoodsBinding
import ru.brauer.catalogofgoods.domain.AppState

class CatalogOfGoodsAdapter(
    private val viewModel: CatalogOfGoodsViewModel,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<CatalogOfGoodsAdapter.ViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        viewModel.observe(lifecycleOwner, ::updateShow)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateShow(appState: AppState) {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = viewModel.getItemCount()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogOfGoodsAdapter.ViewHolder =
        ItemGoodsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)

    override fun onBindViewHolder(holder: CatalogOfGoodsAdapter.ViewHolder, position: Int) =
        holder.bindData(position)


    inner class ViewHolder(private val binding: ItemGoodsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            viewModel.getDataAtPosition(position)
                ?.let {
                    binding.goodsName.text = it.name
                }
        }
    }
}