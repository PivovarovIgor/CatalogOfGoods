package ru.brauer.catalogofgoods.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Goods(val name: String, val photoUrl: String) : Parcelable {
    companion object {
        fun empty() = Goods("", "")
    }
}
