package ru.antonc.weather.ui.main

import android.location.Location
import ru.antonc.weather.repository.WeatherRepository
import ru.antonc.weather.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: WeatherRepository
) : BaseViewModel() {

    fun saveLastLocation(location: Location) {
        repository.saveLastLocation(location)
    }
}