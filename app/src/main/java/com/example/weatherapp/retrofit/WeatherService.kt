package com.example.weatherapp.retrofit

import com.example.weatherapp.model.Geocode
import com.example.weatherapp.model.WeatherModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") appId: String
    ): Response<WeatherModel>

    @GET("geo/1.0/direct")
    suspend fun performGeocoding(
        @Query("q") city: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") appId: String
    ): Response<List<Geocode>>
}