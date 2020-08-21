package ru.antonc.weather.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import ru.antonc.weather.data.entities.WeatherData

@Dao
abstract class WeatherDAO : BaseDAO<WeatherData> {

    @Query("SELECT * from ${WeatherData.TABLE_NAME}")
    abstract fun getData(): Flowable<WeatherData>

    @Query("DELETE FROM ${WeatherData.TABLE_NAME}")
    abstract fun clearTable()

    @Transaction
    open fun updateData(weatherData: WeatherData) {
        clearTable()
        insert(weatherData)
    }

}