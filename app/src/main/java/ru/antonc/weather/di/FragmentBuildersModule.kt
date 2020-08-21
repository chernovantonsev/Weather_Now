package ru.antonc.weather.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.antonc.weather.ui.weather.WeatherFragment


@Suppress("unused")
@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeWeatherFragment(): WeatherFragment
}
