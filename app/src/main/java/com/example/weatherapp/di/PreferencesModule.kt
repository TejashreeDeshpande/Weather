package com.example.weatherapp.di

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherapp.constants.Constants
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class SharedPreferencesModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(Constants.PREF_WEATHER_APP, Context.MODE_PRIVATE)
    }
}