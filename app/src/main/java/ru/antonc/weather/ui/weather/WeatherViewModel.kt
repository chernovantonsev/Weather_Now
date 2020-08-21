package ru.antonc.weather.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import ru.antonc.weather.data.entities.WeatherData
import ru.antonc.weather.repository.WeatherRepository
import ru.antonc.weather.ui.base.BaseViewModel
import javax.inject.Inject

class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : BaseViewModel() {


    val weatherData: LiveData<WeatherData> =
        LiveDataReactiveStreams.fromPublisher(repository.weatherData)


    fun updateData() {
        repository.updateWeatherData()
    }

}