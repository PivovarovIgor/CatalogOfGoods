package ru.brauer.catalogofgoods.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class Settings<T> {
    abstract fun getSettingsData(): StateFlow<T>
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

    private var settingsDataFlow: MutableStateFlow<FtpSettingsData> =
        MutableStateFlow(FtpSettingsData.emptyData())

    init {
        loadSettingsData()
    }

    override fun getSettingsData(): StateFlow<FtpSettingsData> = settingsDataFlow

    override fun setSettingsData(settingData: FtpSettingsData) {
        settingsDataFlow.value = settingData
    }

    private fun loadSettingsData() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
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
                settingsDataFlow.value = data.await()
            }
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