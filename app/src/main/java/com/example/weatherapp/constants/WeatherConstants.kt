package com.example.weatherapp.constants

import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val PREF_CITY = "city"
    const val PREF_UNITS = "units"
    const val PREF_WEATHER_APP = "weatherapp"
    const val BASE_URL = "https://api.openweathermap.org/"
    const val IMAGE_URL = "https://openweathermap.org/img/wn/IMAGE.png"
    const val IMPERIAL = "imperial"

    const val OPEN_WEATHER_APP_ID = "51645f4c922c86f53879b3525c6ec489"

    fun currentDateTime(milliseconds: Long): String {
        return SimpleDateFormat("yyyy, MMM dd  hh:mm a").format(Date(milliseconds)).toString()
    }
    fun todayDate(): String {
        val formatter = SimpleDateFormat("yyyy, MMM dd   hh:mm a", Locale("US"))
        val date = Date()
        return formatter.format(date)
    }
}