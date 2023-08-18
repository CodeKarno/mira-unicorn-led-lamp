package com.procrastinationcollaboration.miraunicornledlamp.services

import com.procrastinationcollaboration.miraunicornledlamp.services.dto.LedLampStateDto
import com.procrastinationcollaboration.miraunicornledlamp.services.dto.ModesDto
import com.procrastinationcollaboration.miraunicornledlamp.services.dto.StatusDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit
    .Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(Consts.LAMP_SERVER_BASE_URL)
    .build()

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
    val apiService: LedLampApiService by lazy {
        retrofit.create(LedLampApiService::class.java)
    }
}