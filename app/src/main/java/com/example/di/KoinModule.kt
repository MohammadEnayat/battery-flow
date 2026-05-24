package com.example.di

import com.example.data.local.BatteryDatabase
import com.example.data.remote.BatteryTipsApi
import com.example.data.remote.MockInterceptor
import com.example.data.repository.BatteryRepositoryImpl
import com.example.domain.repository.BatteryRepository
import com.example.presentation.viewmodel.BatteryViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {
    // Room database and DAO
    single { BatteryDatabase.getDatabase(androidContext()) }
    single { get<BatteryDatabase>().batterySessionDao() }

    // Moshi JSON Converter
    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // OkHttpClient with MockInterceptor
    single {
        OkHttpClient.Builder()
            .addInterceptor(MockInterceptor())
            .build()
    }

    // Retrofit Client
    single {
        Retrofit.Builder()
            .baseUrl("https://api.mockbattery.example.com/")
            .client(get<OkHttpClient>())
            .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
            .build()
    }

    // API Service
    single<BatteryTipsApi> {
        get<Retrofit>().create(BatteryTipsApi::class.java)
    }

    // Repository
    single<BatteryRepository> {
        BatteryRepositoryImpl(get(), get())
    }

    // ViewModel
    viewModel {
        BatteryViewModel(androidApplication(), get())
    }
}
