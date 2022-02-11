package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import ru.brauer.catalogofgoods.data.net.FtpClientConnectHelperProvider

@Module
class NetModule {

    @Provides
    fun getFtpClientConnectHelperProvider(): FtpClientConnectHelperProvider =
        FtpClientConnectHelperProvider()
}