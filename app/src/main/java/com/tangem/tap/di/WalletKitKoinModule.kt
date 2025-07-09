package com.tangem.tap.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

val walletKitMoshiModule = module {
    single(qualifier = named("moshi")) {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
    }
}
