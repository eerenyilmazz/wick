package com.wick.app.di

import com.wick.app.BuildConfig
import com.wick.core.network.di.BaseUrl
import com.wick.core.network.di.CertPin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @CertPin
    fun provideCertPin(): String = BuildConfig.CERTIFICATE_PIN
}
