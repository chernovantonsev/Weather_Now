package ru.antonc.weather.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.antonc.weather.data.entities.WeatherData

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromListOfWeather(weatherList: List<WeatherData.Weather>): String {
        return Gson().toJson(weatherList)
    }

    @TypeConverter
    @JvmStatic
    fun toListOfWeather(weatherListString: String): List<WeatherData.Weather> {
        return Gson().fromJson(
            weatherListString,
            object : TypeToken<List<WeatherData.Weather>>() {}.type
        )
    }
}