package com.st.demo.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.st.demo.api_interface.AuthService
import com.st.demo.api_interface.NotificationInterface
import com.st.demo.api_interface.RoadInterface
import com.st.demo.api_interface.SensorDataService
import com.st.demo.exceptions.NoConnectivityException
import com.st.demo.utils.Constants.Companion.BASE_URL
import com.st.demo.utils.Constants.Companion.BASE_URL_ROAD_MANAGEMENT
import com.st.demo.utils.Constants.Companion.BASE_URL_SENSOR_DATA

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

// Qualifiers per i diversi backend
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SensorDataRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RoadManagementRetrofit

// Qualifiers per distinguere il TUO Retrofit da quello di ST Blue SDK
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendOkHttp

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BACKEND_URL_AUTH = BASE_URL
    private const val BACKEND_URL_SENS = BASE_URL_SENSOR_DATA
    private const val BACKEND_URL_ROAD = BASE_URL_ROAD_MANAGEMENT
    @Provides
    @Singleton
    @BackendOkHttp // AGGIUNTO
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


    // Retrofit per Auth (porta 8080)
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(@BackendOkHttp okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(BACKEND_URL_AUTH) // porta 8080
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // Retrofit per SensorData (porta 8006)
    @Provides
    @Singleton
    @SensorDataRetrofit
    fun provideSensorDataRetrofit(@BackendOkHttp okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(BACKEND_URL_SENS) // porta 8006
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    // Retrofit per RoadManagemnt (porta 8006)
    @Provides
    @Singleton
    @RoadManagementRetrofit
    fun provideRoadManagementRetrofit(@BackendOkHttp okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BACKEND_URL_ROAD)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }


    // Services
    @Provides
    @Singleton
    fun provideAuthService(@AuthRetrofit retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideSensorDataService(@SensorDataRetrofit retrofit: Retrofit): SensorDataService {
        return retrofit.create(SensorDataService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationService(@RoadManagementRetrofit retrofit: Retrofit): RoadInterface {
        return retrofit.create(RoadInterface::class.java)
    }

    @Provides
    @Singleton
    @Named("ConnectivityInterceptor")
    fun provideConnectivityInterceptor(@ApplicationContext context: Context): Interceptor =
        Interceptor { chain ->
            if (!isNetworkAvailable(context)) {
                throw NoConnectivityException("No internet connection")
            }
            chain.proceed(chain.request())
        }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
