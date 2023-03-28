package com.example.weatherapp.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.*
import com.example.weatherapp.model.Geocode
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.model.WeatherUnits
import com.example.weatherapp.repository.WeatherRepository
import timber.log.Timber

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    // stores the current current city
    var currentCity = MutableLiveData<String>()

    // stores the geo location of current city
    var currentGeoCode = MutableLiveData<Geocode>()

    var units = MutableLiveData<WeatherUnits>().apply { WeatherUnits.IMPERIAL }

    val weatherResponse: LiveData<WeatherModel?> = currentGeoCode.switchMap {
        it?.let { geocode ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                try {
                    repository.getWeatherData(
                        lat = geocode.lat,
                        lon = geocode.lon,
                        units = units.value?.type ?: WeatherUnits.IMPERIAL.type
                    ).also { response ->
                        emit(response)
                    }
                } catch (exception: Exception) {
                    Timber.d("Network error")
                }
            }
        }
    }

    val geocodingResponse: LiveData<List<Geocode>?> = currentCity.switchMap {
        it?.let { city ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
//                try {
                    repository.performGeocoding(
                        city = city
                    ).also { response ->
                        Timber.d("$response")
                        emit(response)
                    }
//                } catch (exception: Exception) {
//                    Timber.d("Network error")
//                }
            }
        }
    }

    fun formatCity(text: String): String? {
        val words = text.split(",").toTypedArray()
        return when (words.size) {
            1 -> words[0]
            3 -> "${words[0]},${words[1]},${words[2]}"
            else -> null
        }
    }

    fun refreshCurrentCity(name: String) {
        currentCity.postValue(name)
    }
}