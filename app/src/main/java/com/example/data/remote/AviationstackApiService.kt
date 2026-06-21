package com.example.data.remote

import com.example.data.model.AviationstackResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface AviationstackApiService {

    @GET("v1/flights")
    suspend fun getFlights(
        @Query("access_key") apiKey: String,
        @Query("flight_number") flightNumber: String? = null,
        @Query("dep_iata") departureIata: String? = null,
        @Query("arr_iata") arrivalIata: String? = null,
        @Query("limit") limit: Int = 100
    ): AviationstackResponse

    companion object {
        fun create(baseUrl: String): AviationstackApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // High timeout for dynamic networks
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            // Ensure baseUrl ends with '/'
            val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(formattedBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(AviationstackApiService::class.java)
        }
    }
}
