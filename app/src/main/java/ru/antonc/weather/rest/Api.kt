package ru.antonc.weather.rest

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.antonc.weather.data.entities.WeatherData

interface Api {
    @GET("data/2.5/weather")
    fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "",
        @Query("lang") lang: String = ""
    ): Single<WeatherData>
}