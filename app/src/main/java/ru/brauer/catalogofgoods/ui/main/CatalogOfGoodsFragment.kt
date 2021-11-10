package ru.brauer.catalogofgoods.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.brauer.catalogofgoods.databinding.FragmentCatalogOfGoodsBinding

@AndroidEntryPoint
class CatalogOfGoodsFragment : Fragment() {

    companion object {
        fun newInstance() = CatalogOfGoodsFragment()
    }

    private var binding: FragmentCatalogOfGoodsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCatalogOfGoodsBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root



    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}