package ru.brauer.catalogofgoods.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

abstract class Settings<T> {
    abstract fun getSettingsData(): T
    abstract fun setSettingsData(settingData: T)
}

class FtpSettings(context: Context) : Settings<FtpSettingsData>() {
    companion object {
        private const val FILE_NAME = "FtpSettings"

        private const val VALUE_HOST_ADDRESS = "ftp_host_address"
        private const val VALUE_LOGIN = "ftp_login"
        private const val VALUE_PATH = "ftp_path"
        private const val VALUE_PASSWORD = "ftp_password"
    }

    private val sharedPreference: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private var settingsData: FtpSettingsData = FtpSettingsData.emptyData()


    override fun getSettingsData(): FtpSettingsData {
        TODO("Not yet implemented")
    }

    override fun setSettingsData(settingData: FtpSettingsData) {
        TODO("Not yet implemented")
    }

    private suspend fun loadSettingsData() = coroutineScope {
        val settingsRaw = sharedPreference.all
        val data = async(Dispatchers.IO) {
            settingsRaw?.let {
                FtpSettingsData(
                    hostAddress = it[VALUE_HOST_ADDRESS] as? String ?: "",
                    path = it[VALUE_PATH] as? String ?: "",
                    login = it[VALUE_LOGIN] as? String ?: "",
                    password = it[VALUE_PASSWORD] as? String ?: ""
                )
            } ?: FtpSettingsData.emptyData()
        }
        withContext(Dispatchers.Main) {
            settingsData = data.await()
        }
    }
}

data class FtpSettingsData(
    val hostAddress: String,
    val path: String,
    val login: String,
    val password: String
) {
    companion object {
        fun emptyData() = FtpSettingsData("", "", "", "")
    }
}

class CommerceMLSettings {
    companion object {
        private const val ID_GOODS = "commerceML_goods_id"
        private const val ID_TYPE_PRICE = "commerceML_type_price"
    }
}