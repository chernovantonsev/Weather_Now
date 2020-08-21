package ru.antonc.weather

import android.app.Application
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.antonc.weather.di.AppInjector
import javax.inject.Inject

class App : Application(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()

        AppInjector.init(this)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}

@GlideModule
class MyAppGlideModule : AppGlideModule()