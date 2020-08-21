package ru.antonc.weather.repository

import android.location.Location
import androidx.room.EmptyResultSetException
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import ru.antonc.weather.common.API_KEY
import ru.antonc.weather.data.AppDatabase
import ru.antonc.weather.data.entities.Lang
import ru.antonc.weather.data.entities.Units
import ru.antonc.weather.data.entities.WeatherData
import ru.antonc.weather.rest.Api
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WeatherRepository @Inject constructor(
    private val api: Api,
    private val database: AppDatabase,
    private val dataDisposable: CompositeDisposable
) {

    val weatherData: Flowable<WeatherData> = database.weatherDataDAO().getData()
        .doOnNext { weatherData -> lastUpdatedTimeStamp.accept(weatherData.dt) }

    private val lastUpdatedTimeStamp: BehaviorRelay<Long> = BehaviorRelay.createDefault(0)

    init {
        Flowables.combineLatest(
            Flowable.interval(0, 10, TimeUnit.MINUTES),
            lastUpdatedTimeStamp.toFlowable(BackpressureStrategy.LATEST)
                .debounce(1, TimeUnit.SECONDS)

        ) { _, timeStamp -> timeStamp }
            .filter { timeStamp ->
                kotlin.math.abs(timeStamp - Calendar.getInstance().timeInMillis / 1000) >
                        TimeUnit.MINUTES.toMillis(10)
            }
            .subscribe({
                updateWeatherData()
            }, {})
            .addTo(dataDisposable)
    }


    fun saveLastLocation(location: Location) {
        Completable.create {
            database.locationDAO().updateData(
                ru.antonc.weather.data.entities.Location(
                    location.longitude,
                    location.latitude
                )
            )

            it.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
            .addTo(dataDisposable)
    }

    fun updateWeatherData() {
        database.locationDAO().getLocation()
            .onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    Single.just(ru.antonc.weather.data.entities.Location(0.0, 0.0))
                } else {
                    Single.error(it)
                }
            }
            .flatMap { location ->
                api.getWeatherData(
                    location.lat,
                    location.lon,
                    API_KEY,
                    Units.METRIC.value,
                    Lang.RU.code
                )
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ weatherData ->
                database.weatherDataDAO().updateData(weatherData)
            }, { })
            .addTo(dataDisposable)
    }


}