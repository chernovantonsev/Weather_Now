package ru.antonc.weather.ui.main

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import ru.antonc.weather.repository.WeatherRepository
import ru.antonc.weather.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: WeatherRepository
) : BaseViewModel() {

    val isNeedUpdateLocationSetting: LiveData<Boolean> =
        LiveDataReactiveStreams.fromPublisher(repository.isNeedUpdateLocationSetting)

    fun saveLastLocation(location: Location) {
        repository.saveLastLocation(location)
    }
}