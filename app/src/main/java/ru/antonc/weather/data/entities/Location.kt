package ru.antonc.weather.data.entities

import androidx.room.Entity

@Entity(tableName = Location.TABLE_NAME, primaryKeys = ["lon", "lat"])
data class Location(
    var lon: Double = 0.0,
    var lat: Double = 0.0
) {
    companion object {
        const val TABLE_NAME = "location_table"
    }
}