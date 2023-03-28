package com.example.weatherapp.repository

import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.constants.Constants
import com.example.weatherapp.model.Geocode
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.retrofit.WeatherService
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val weatherService: WeatherService) {

    private val _geocode = MutableLiveData<List<Geocode>>()
    private val _weather = MutableLiveData<WeatherModel>()

    suspend fun performGeocoding(city: String): List<Geocode>? {
        val result =
            weatherService.performGeocoding(city = city, appId = Constants.OPEN_WEATHER_APP_ID)
        return if (result.isSuccessful && result.body() != null) {
            _geocode.postValue(result.body())
            result.body()
        } else null
    }

    suspend fun getWeatherData(
        lat: Double,
        lon: Double,
        units: String = Constants.IMPERIAL
    ): WeatherModel? {
        val result = weatherService.getWeatherData(
            lat = lat,
            lon = lon,
            units = units,
            appId = Constants.OPEN_WEATHER_APP_ID
        )
        return if (result.isSuccessful && result.body() != null) {
            _weather.postValue(result.body())
            result.body()
        } else {
            null
        }
    }

}