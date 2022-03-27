package ru.brauer.catalogofgoods.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.brauer.catalogofgoods.data.settings.FtpSettings
import ru.brauer.catalogofgoods.data.settings.FtpSettingsData
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val settings: FtpSettings
) : ViewModel() {

    private val _liveData: MutableLiveData<FtpSettingsData> = MutableLiveData()
    val liveData: LiveData<FtpSettingsData>
        get() = _liveData

    init {
        viewModelScope.launch {
            settings.getSettingsData().collect {
                _liveData.postValue(it)
            }
        }
    }

    fun saveSettings(settingData: FtpSettingsData) {
        settings.setSettingsData(settingData)
    }
}