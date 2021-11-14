package ru.brauer.catalogofgoods.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Goods(val id: String, val name: String, val photoUrl: String) : Parcelable {

    constructor(name: String, photoUrl: String) : this("", name, photoUrl)

    companion object {
        fun empty() = Goods("", "", "")
    }
}
