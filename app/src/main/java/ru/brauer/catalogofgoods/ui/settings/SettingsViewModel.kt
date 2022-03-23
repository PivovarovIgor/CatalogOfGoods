package ru.brauer.catalogofgoods.ui.settings

import androidx.lifecycle.ViewModel
import ru.brauer.catalogofgoods.data.settings.FtpSettings
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val settings: FtpSettings
) : ViewModel() {

    fun startVM() {

    }
}