package com.example.weatherapp.model

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class WeatherModel(
    val coord: Coordinates?,
    val weather: List<Weather>?,
    val main: MainTemp?,
    val name: String?,
    val dt: Long?,
    val sys: Sys
)

data class Coordinates(val lat: Double, val lon: Double)

data class Weather(
    val id: String?,
    val main: String?,
    val description: String?,
    val icon: String?
)

data class MainTemp(
    val temp: Double?,
    @SerializedName("feels_like")
    val feelsLike: Double?,
    @SerializedName("temp_min")
    val tempMin: Double?,
    @SerializedName("temp_max")
    val tempMax: Double?,
    val pressure: Double?,
    val humidity: Double?,
)

data class Sys(
    val type: Int?,
    val id: Int?,
    val country: String?,
    val sunrise: Long?,
    val sunset: Long?
)