package com.wick.core.billing.di

import com.wick.core.billing.BillingClientGateway
import com.wick.core.billing.GooglePlayBillingClientGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * The single place that decides which store/SDK backs [BillingClientGateway].
 * To swap Google Play Billing for another provider, write a new implementation of
 * [BillingClientGateway] and change this one binding — no other code in the app
 * (core:data, ViewModels, screens) needs to know or change.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    abstract fun bindBillingClientGateway(impl: GooglePlayBillingClientGateway): BillingClientGateway
}
