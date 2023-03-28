package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.ApplicationComponent
import com.example.weatherapp.di.DaggerApplicationComponent
import com.example.weatherapp.di.SharedPreferencesModule

class WeatherApplication : Application() {
    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder()
            .sharedPreferencesModule(SharedPreferencesModule(applicationContext))
            .build()
    }
}