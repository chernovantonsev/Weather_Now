package ru.antonc.weather.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import ru.antonc.weather.common.IMAGE_URL

@Entity(tableName = WeatherData.TABLE_NAME, primaryKeys = ["id"])
data class WeatherData(
    @Embedded
    var coord: Location? = null,
    var weather: List<Weather> = emptyList(),
    var base: String? = null,
    @Embedded
    var main: Main? = null,
    @Embedded
    var wind: Wind? = null,
    @Embedded
    var clouds: Clouds? = null,
    var dt: Long = 0,
    @Embedded(prefix = "sys_")
    var sys: Sys? = null,
    var timezone: Int = 0,
    var id: Int = 0,
    var name: String = "",
    var cod: Int = 0
) {
    companion object {
        const val TABLE_NAME = "weather_data_table"
    }

    fun getWeatherInfo(): Weather {
        return if (weather.isNotEmpty())
            weather.first()
        else Weather()
    }

    fun getWeatherIconPath(): String {
        with(getWeatherInfo()) {
            if (icon.isNotEmpty())
                return IMAGE_URL + icon
        }
        return ""
    }

    class Main {
        var temp = 0.0

        @SerializedName("feels_like")
        var feelsLike = 0.0

        @SerializedName("temp_min")
        var tempMin = 0.0

        @SerializedName("temp_max")
        var tempMax = 0.0
        var pressure = 0
        var humidity = 0
    }

    class Wind {
        var speed = 0.0
        var deg = 0.0
    }

    class Weather {
        var id = 0
        var main: String = ""
        var description: String = ""
        var icon: String = ""
    }

    class Sys {
        var type = 0
        var id = 0
        var message = 0.0
        var country: String = ""
        var sunrise = 0
        var sunset = 0
    }

    class Clouds {
        var all = 0
    }
}


