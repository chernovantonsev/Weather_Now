package ru.antonc.weather.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.antonc.weather.ui.main.MainActivity

@Suppress("unused")
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity
}
