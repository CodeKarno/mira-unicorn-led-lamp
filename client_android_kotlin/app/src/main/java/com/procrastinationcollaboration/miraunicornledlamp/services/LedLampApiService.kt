package com.procrastinationcollaboration.miraunicornledlamp.services

import com.procrastinationcollaboration.miraunicornledlamp.services.dto.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface LedLampApiService {
    @GET("modes")
    suspend fun getModes(): ModesDto

    @FormUrlEncoded
    @POST("state")
    suspend fun changeState(
        @Field("mode") mode: String?,
        @Field("color") color: String?,
        @Field("brightness") brightness: String?
    ): StatusDto

    @GET("state")
    suspend fun getState(): LedLampStateDto

    @POST("reset")
    suspend fun reset(): StatusDto
}

object LedLamp {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
        .build()
    private var retrofit = Retrofit
        .Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(Consts.LAMP_SERVER_BASE_URL)
        .build()

    fun getApiService(newBaseUrl: String? = null): LedLampApiService {
        if (newBaseUrl != retrofit.baseUrl().toString()) {
            retrofit = retrofit
                .newBuilder()
                .baseUrl(newBaseUrl ?: Consts.LAMP_SERVER_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }
        return retrofit.create(LedLampApiService::class.java)
    }
}