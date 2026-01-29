package com.st.demo.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.st.demo.api_interface.AuthService
import com.st.demo.api_interface.NotificationInterface
import com.st.demo.api_interface.SensorDataService
import com.st.demo.exceptions.NoConnectivityException
import com.st.demo.utils.Constants.Companion.BASE_URL

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
    private const val BACKEND_URL = BASE_URL

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

    @Provides
    @Singleton
    @BackendRetrofit // AGGIUNTO
    fun provideRetrofit(@BackendOkHttp okHttpClient: OkHttpClient): Retrofit { // MODIFICATO
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
        }
        return Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
    @Provides
    @Singleton
    fun provideAuthService(@BackendRetrofit retrofit: Retrofit): AuthService { // MODIFICATO
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideSensorDataService(@BackendRetrofit retrofit: Retrofit): SensorDataService { // MODIFICATO
        return retrofit.create(SensorDataService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationService(@BackendRetrofit retrofit: Retrofit): NotificationInterface { // MODIFICATO
        return retrofit.create(NotificationInterface::class.java)
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

    // Helper function to check network availability
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
