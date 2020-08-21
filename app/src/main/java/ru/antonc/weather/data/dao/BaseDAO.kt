package ru.antonc.weather.data.dao


import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import io.reactivex.Completable

interface BaseDAO<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T)

    @Delete
    fun delete(vararg obj: T)

    @Update
    fun update(vararg obj: T)

}