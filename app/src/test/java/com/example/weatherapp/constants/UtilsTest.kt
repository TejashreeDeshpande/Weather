package com.example.weatherapp.constants

import com.example.weatherapp.constants.Constants.IMAGE_URL
import org.junit.Assert
import org.junit.Test

class UtilsTest {

    @Test
    fun testFormatImageUrl() {
        val updatedText = IMAGE_URL.formatImageUrl("1d")
        Assert.assertEquals("https://openweathermap.org/img/wn/1d.png", updatedText)
    }
}