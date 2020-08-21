package ru.antonc.weather.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.antonc.weather.BuildConfig
import ru.antonc.weather.common.BASE_URL
import ru.antonc.weather.data.AppDatabase
import ru.antonc.weather.rest.Api
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

//
//    @Singleton
//    @Provides
//    fun provideHttpClient(): OkHttpClient {
//        val httpClient = OkHttpClient.Builder()
//
//        if (BuildConfig.DEBUG) {
//            val logging = HttpLoggingInterceptor()
//            logging.level = HttpLoggingInterceptor.Level.BODY
//            httpClient.addInterceptor(logging)
//        }
//
//        return httpClient.build()
//    }
//
//    @Singleton
//    @Provides
//    fun provideRestartService(httpClient: OkHttpClient): Api {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .client(httpClient)
//            .build()
//            .create(Api::class.java)
//    }
}
