package com.example.weatherapp.constants

import org.junit.Assert
import org.junit.Test

class WeatherConstantsTest {
    @Test
    fun testCurrentTimeDate() {
        val timeDate = Constants.currentDateTime(1680017639728)
        Assert.assertNotEquals("", timeDate)
    }
}