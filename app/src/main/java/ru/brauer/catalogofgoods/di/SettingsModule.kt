package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.App
import ru.brauer.catalogofgoods.data.settings.FtpSettings
import javax.inject.Singleton

@Module
class SettingsModule {

    @Singleton
    @Provides
    fun getFtpSettings(context: App): FtpSettings = FtpSettings(context)
}