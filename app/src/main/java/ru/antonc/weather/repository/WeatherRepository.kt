package ru.antonc.weather.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.EmptyResultSetException
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.antonc.weather.R
import ru.antonc.weather.common.API_KEY
import ru.antonc.weather.common.PreferencesConstants
import ru.antonc.weather.data.AppDatabase
import ru.antonc.weather.data.entities.EventContent
import ru.antonc.weather.data.entities.Lang
import ru.antonc.weather.data.entities.Units
import ru.antonc.weather.data.entities.WeatherData
import ru.antonc.weather.rest.Api
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WeatherRepository @Inject constructor(
    private val api: Api,
    private val database: AppDatabase,
    private val preferences: SharedPreferences,
    private val dataDisposable: CompositeDisposable
) {

    val weatherData: Flowable<WeatherData> = database.weatherDataDAO().getData()
        .doOnNext { weatherData -> _lastUpdatedTimeStamp.accept(weatherData.dt) }

    private val _progress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = _progress

    private val _errorMessage = MutableLiveData<EventContent<Int>>()
    val errorMessage: LiveData<EventContent<Int>> = _errorMessage

    private val _lastUpdatedTimeStamp: BehaviorRelay<Long> = BehaviorRelay.createDefault(0)
    private val lastUpdatedTimeStamp = _lastUpdatedTimeStamp.toFlowable(BackpressureStrategy.LATEST)
        .debounce(1, TimeUnit.SECONDS)

    private val prefSubject = BehaviorSubject.createDefault(preferences)

    private val prefChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            prefSubject.onNext(sharedPreferences)
        }

    val isNeedUpdateLocationSetting: Flowable<Boolean> = prefSubject
        .map { it.getBoolean(PreferencesConstants.IS_LOCATION_UPDATE_NEED, true) }
        .toFlowable(BackpressureStrategy.LATEST)

    private var checkLocationDisposable: Disposable? = null

    init {
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener)

        checkLocationDisposable = database.locationDAO().getLocationFlowable()
            .doOnNext {
                runCheckingForUpdate()
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                checkLocationDisposable?.let {
                    dataDisposable.remove(it)
                    checkLocationDisposable = null
                }
            }, {})
            .addTo(dataDisposable)
    }


    private fun runCheckingForUpdate() {
        Flowables.combineLatest(
            Flowable.interval(0, 10, TimeUnit.MINUTES),
            lastUpdatedTimeStamp
        ) { _, timeStamp -> timeStamp }
            .filter { timeStamp ->
                kotlin.math.abs(timeStamp * 1000 - Calendar.getInstance().timeInMillis) >
                        TimeUnit.MINUTES.toMillis(10)
            }
            .subscribeOn(Schedulers.computation())
            .subscribe({
                updateWeatherData(true)
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
            .subscribe({}, {})
            .addTo(dataDisposable)
    }

    fun updateWeatherData(isQuiet: Boolean = false) {
        if (_progress.value == true)
            return

        if (!isQuiet)
            _progress.postValue(true)

        database.locationDAO().getLocation()
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
                _progress.postValue(false)
            }, { error ->
                _progress.postValue(false)

                if (!isQuiet)
                    _errorMessage.postValue(
                        when (error) {
                            is UnknownHostException -> EventContent(R.string.error_no_connection)
                            is EmptyResultSetException -> EventContent(R.string.error_no_gps)
                            else -> EventContent(R.string.error_while_update)
                        }
                    )
            })
            .addTo(dataDisposable)
    }

    fun inverseLocationPreferenceValue() {
        preferences.getBoolean(PreferencesConstants.IS_LOCATION_UPDATE_NEED, false).let { value ->
            saveBoolean(PreferencesConstants.IS_LOCATION_UPDATE_NEED, !value)
                .subscribe()
                .addTo(dataDisposable)
        }
    }

    private fun Single<SharedPreferences>.editSharedPreferences(batch: SharedPreferences.Editor.() -> Unit): Completable =
        flatMapCompletable {
            Completable.fromAction {
                it.edit().also(batch).apply()
            }
        }

    private fun saveBoolean(key: String, value: Boolean): Completable = prefSubject
        .firstOrError()
        .editSharedPreferences {
            putBoolean(key, value)
        }

}