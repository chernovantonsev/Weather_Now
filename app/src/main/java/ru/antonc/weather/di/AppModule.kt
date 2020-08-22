package ru.antonc.weather.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import ru.antonc.weather.data.AppDatabase
import javax.inject.Singleton


@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Singleton
    @Provides
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Singleton
    @Provides
    fun provideSharedPreference(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}
