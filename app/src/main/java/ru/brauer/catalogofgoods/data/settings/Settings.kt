package ru.brauer.catalogofgoods.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

abstract class Settings<T> {
    abstract fun getSettingsData(): Flow<T>
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

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var jobWriting: Job? = null
    private var jobLoadingPreference: Job? = null

    private val sharedPreference: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private var settingsDataFlow: MutableStateFlow<FtpSettingsData> =
        MutableStateFlow(FtpSettingsData.emptyData())

    init {
        sharedPreference.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == VALUE_HOST_ADDRESS
                || key == VALUE_LOGIN
                || key == VALUE_PATH
                || key == VALUE_PASSWORD
            ) {
                loadSettingsData()
            }
        }
        loadSettingsData()
    }

    override fun getSettingsData(): Flow<FtpSettingsData> = settingsDataFlow
        .distinctUntilChanged { old, new ->
            old == new
        }

    override fun setSettingsData(settingData: FtpSettingsData) {
        writeSettingsData(settingData)
        settingsDataFlow.value = settingData
    }

    private fun writeSettingsData(settingData: FtpSettingsData) {
        jobWriting?.cancel()
        jobWriting = scope.launch {
            sharedPreference.edit {
                putString(VALUE_HOST_ADDRESS, settingData.hostAddress)
                putString(VALUE_PATH, settingData.path)
                putString(VALUE_LOGIN, settingData.login)
                putString(VALUE_PASSWORD, settingData.password)
            }
        }
    }

    private fun loadSettingsData() {
        jobLoadingPreference?.cancel()
        jobLoadingPreference = scope.launch(Dispatchers.IO) {
            delay(100)
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
            settingsDataFlow.value = data.await()
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