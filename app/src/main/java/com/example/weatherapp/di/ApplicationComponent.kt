package com.example.weatherapp.di

import com.example.weatherapp.ui.HomeFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SharedPreferencesModule::class, NetworkModule::class])
interface ApplicationComponent {
    fun inject(fragment: HomeFragment)
}