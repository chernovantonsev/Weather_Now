package ru.antonc.weather.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.antonc.weather.data.dao.LocationDAO
import ru.antonc.weather.data.dao.WeatherDAO
import ru.antonc.weather.data.entities.Location
import ru.antonc.weather.data.entities.WeatherData

@Database(
    version = 1,
    entities = [WeatherData::class, Location::class],
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun weatherDataDAO(): WeatherDAO
    abstract fun locationDAO(): LocationDAO

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context, AppDatabase::class.java,
                context.packageName
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}