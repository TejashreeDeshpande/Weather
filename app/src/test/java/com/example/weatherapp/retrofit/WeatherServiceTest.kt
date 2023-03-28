package com.example.weatherapp.retrofit

import com.example.weatherapp.Helper
import com.example.weatherapp.model.Geocode
import com.example.weatherapp.model.WeatherModel
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherServiceTest {

    private lateinit var mockWebServer : MockWebServer
    private lateinit var api: WeatherService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        api = Retrofit.Builder().baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherService::class.java)

    }
    @Test
    fun testGetGeocodeValid() = runTest {
        val mockResponse = MockResponse()
        val content = Helper.readFileResource("/geocoding_fremont.json")
        mockResponse.setResponseCode(200)
        mockResponse.setBody(content)
        mockWebServer.enqueue(mockResponse)

        val response : Response<List<Geocode>> = api.performGeocoding("Fremont", appId = "12345")
        mockWebServer.takeRequest()

        Assert.assertEquals(false, response.body()!!.isEmpty())
        Assert.assertEquals(1, response.body()!!.size)
    }

    @Test
    fun testGetGeocodeEmpty() = runTest {
        val mockResponse = MockResponse()
        val content = Helper.readFileResource("/geocoding_empty.json")
        mockResponse.setResponseCode(200)
        mockResponse.setBody(content)
        mockWebServer.enqueue(mockResponse)

        val response : Response<List<Geocode>> = api.performGeocoding("Fremon", appId = "12345")
        mockWebServer.takeRequest()

        Assert.assertEquals(true, response.body()!!.isEmpty())
    }

    @Test
    fun testGetWeatherDataValid() = runTest {
        val mockResponse = MockResponse()
        val content = Helper.readFileResource("/weather_fremont.json")
        mockResponse.setResponseCode(200)
        mockResponse.setBody(content)
        mockWebServer.enqueue(mockResponse)

        val response : Response<WeatherModel> = api.getWeatherData(lat = 37.5482697,
            lon = -121.988571,"imperial", appId = "12345")
        mockWebServer.takeRequest()

        Assert.assertEquals(false, response.body()?.weather!!.isEmpty())
    }

    @Test
    fun testGetWeatherDataEmpty() = runTest {
        val mockResponse = MockResponse()
        val content = Helper.readFileResource("/weather_empty.json")
        mockResponse.setResponseCode(200)
        mockResponse.setBody(content)
        mockWebServer.enqueue(mockResponse)

        val response : Response<WeatherModel> = api.getWeatherData(lat = 37.5482697,
            lon = -121.988571,"imperial", appId = "12345")
        mockWebServer.takeRequest()

        Assert.assertNull(response.body()!!.weather)
    }

    fun tearDown() {
        mockWebServer.shutdown()
    }
}