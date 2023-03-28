package com.example.weatherapp.ui

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.constants.*
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.model.Geocode
import com.example.weatherapp.model.WeatherModel
import com.example.weatherapp.model.WeatherUnits
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory
import com.google.android.gms.location.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val PERMISSION_ID = 1000
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    lateinit var viewModel: WeatherViewModel

    @Inject
    lateinit var preferences : SharedPreferences

    @Inject
    lateinit var weatherViewModelFactory: WeatherViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            (it.application as WeatherApplication).applicationComponent.inject(this)
            viewModel =
                ViewModelProvider(this, weatherViewModelFactory)[WeatherViewModel::class.java]
        }
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        activity?.let {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(it)
        }
        getLastLocation()

        setUpOnClickListeners()
        observeAPIResponse()
    }

    private fun readPreferences() {
        activity?.let {
            preferences.getString(Constants.PREF_CITY, null)?.let { city ->
                viewModel.currentCity.postValue(city)
            }
            preferences.getString(Constants.PREF_UNITS, null)?.let { units ->
                viewModel.units.postValue(if (units == Constants.IMPERIAL) WeatherUnits.IMPERIAL else WeatherUnits.METRIC)
            }
        }
    }
    private fun setUpOnClickListeners() {
        binding.unitsSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            viewModel.units.postValue(if (isChecked) WeatherUnits.IMPERIAL else WeatherUnits.METRIC)
            // refresh screen with updated units
            binding.cityNameTextView.text?.toString()?.let { text ->
                if (text.isNotEmpty())
                    viewModel.refreshCurrentCity(text)
            }
        }

        binding.citySearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchCity(query)
                binding.citySearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.searchButton.setOnClickListener {
            searchCity(binding.citySearchView.query.toString())
            binding.citySearchView.clearFocus()
        }
    }

    private fun searchCity(query: String?) {
        query?.let { text ->
            viewModel.formatCity(text)?.let { cityName ->
                viewModel.refreshCurrentCity(cityName)
            } ?: displayAlert(text)
        } ?: displayAlert("")
    }

    private fun observeAPIResponse() {

        viewModel.weatherResponse.observe(viewLifecycleOwner) { weather ->
            weather?.let { weatherData ->
                Timber.d("$weatherData")
                updateUI(weatherData)
            }
        }
        viewModel.geocodingResponse.observe(viewLifecycleOwner) { geocode ->
            geocode?.let { list ->
                if (list.isNotEmpty()) {
                    viewModel.currentGeoCode.postValue(list.first())
                } else {
                    displayAlert(binding.citySearchView.query.toString())
                }
            }
        }
        viewModel.units.observe(viewLifecycleOwner) { units ->
            units?.let { wu ->
                binding.unitsSwitchButton.isChecked = (wu == WeatherUnits.IMPERIAL)
            }
        }
    }

    private fun updateUI(weatherData: WeatherModel) {
        binding.todayDate.text = weatherData.dt?.let { Constants.todayDate() } ?: ""
        binding.cityNameTextView.text =
            resources.getString(R.string.city_country, weatherData.name, weatherData.sys.country)
        binding.tempTextView.text = resources.getString(
            R.string.temperature,
            weatherData.main?.temp?.roundToInt()?.toString()
        )

        binding.tempFeelsTextView.text = resources.getString(
            R.string.feels_like,
            weatherData.main?.feelsLike?.roundToInt()?.toString()
        )

        binding.hiLowTextView.text = resources.getString(
            R.string.hi_low_temp,
            weatherData.main?.tempMin?.roundToInt()?.toString(),
            weatherData.main?.tempMax?.roundToInt()?.toString()
        )

        binding.tempDetailsTextView.text = weatherData.weather?.first()?.description

        // Icon
        weatherData.weather?.first()?.icon?.let { imageCode ->
            Glide.with(this)
                .load(Constants.IMAGE_URL.formatImageUrl(imageCode))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.weatherIcon)
        }
        // save preference on successful weather data
        preferences.edit().putString(Constants.PREF_CITY, binding.cityNameTextView.text.toString())
            .apply()
        preferences.edit().putString(Constants.PREF_UNITS, viewModel.units.value?.type).apply()
    }

    // Display an alert when data for entered city name not found
    private fun displayAlert(city: String) {

        activity?.let {
            val dialogBuilder = AlertDialog.Builder(it)
            dialogBuilder.setMessage("$city city not found")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    // Clear SearchView Query
                    binding.citySearchView.clearFocus()
                }
            val alert = dialogBuilder.create()
            alert.show()
        }
    }
    // Find current location if Location permission is granted
    private fun getLastLocation() {
        activity?.let {
            if (
                ActivityCompat.checkSelfPermission(
                    it,
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isLocationEnabled()) {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location: Location? = task.result
                        location?.let { loc ->
                            viewModel.currentGeoCode.postValue(
                                Geocode(
                                    lat = loc.latitude,
                                    lon = loc.longitude
                                )
                            )
                        } ?: getNewLocation(it)
                    }
                } else {
                    Toast.makeText(it, "Please enable your location service", Toast.LENGTH_SHORT)
                        .show()
                    readPreferences()
                }
            } else {
                readPreferences()
                startLocationPermissionRequest()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLastLocation()
        } else {
            readPreferences()
        }
    }

    // Ex. Launching ACCESS_FINE_LOCATION permission.
    private fun startLocationPermissionRequest() {
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
    }

    private fun getNewLocation(context: Activity) {
        val locationInterval = 100L
        val locationFastestInterval = 50L
        val locationMaxWaitTime = 100L

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            lastLocation?.let { loc ->
                viewModel.currentGeoCode.postValue(Geocode(lat = loc.latitude, lon = loc.longitude))
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        activity?.let {
            val locationManager: LocationManager =
                it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        return false
    }
}