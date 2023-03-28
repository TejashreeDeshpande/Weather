package com.example.weatherapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Geocode(
    val name: String? = null,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null
): Parcelable