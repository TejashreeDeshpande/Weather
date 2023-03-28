package com.example.weatherapp.constants

fun String.formatImageUrl(data: String) : String {
    return this.replace("IMAGE", data)
}