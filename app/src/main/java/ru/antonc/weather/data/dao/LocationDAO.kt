package ru.antonc.weather.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import ru.antonc.weather.data.entities.Location

@Dao
abstract class LocationDAO : BaseDAO<Location> {

    @Query("SELECT * from ${Location.TABLE_NAME}")
    abstract fun getLocation(): Single<Location>

    @Query("SELECT * from ${Location.TABLE_NAME}")
    abstract fun getLocationFlowable(): Flowable<Location>

    @Query("DELETE FROM ${Location.TABLE_NAME}")
    abstract fun clearTable()

    @Transaction
    open fun updateData(location: Location) {
        clearTable()
        insert(location)
    }
}