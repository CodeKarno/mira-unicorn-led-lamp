package com.procrastinationcollaboration.miraunicornledlamp.services

import android.annotation.SuppressLint
import android.content.Context
import com.procrastinationcollaboration.miraunicornledlamp.R
import com.procrastinationcollaboration.miraunicornledlamp.services.dto.StatusDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory())
    .build()
interface SetupApiService {
    @FormUrlEncoded
    @POST("setup")
    suspend fun setup(
        @Field("ssid") ssid: String,
        @Field("pass") pass: String
    ): StatusDto

    @POST("reset")
    suspend fun reset(): StatusDto // for internal purposes
}


class LampSetup(context: Context) {
    private val okHttpClientBuilder = OkHttpClient.Builder()
    private val retrofitBuilder = Retrofit
        .Builder()
    val apiService: SetupApiService by lazy {
        val client =
            okHttpClientBuilder
                .sslSocketFactory(getSSlConfig(context))
                .hostnameVerifier(StubHostnameVerifier())
                .build()
        val retrofit = retrofitBuilder
            .baseUrl(Consts.TEMP_AP_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
        retrofit.create(SetupApiService::class.java)
    }

    private fun getSSlConfig(context: Context): SSLSocketFactory {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream: InputStream = context.resources.openRawResource(R.raw.cert1)
        val ca: Certificate = cf.generateCertificate(inputStream)

        val keyStoreType: String = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmfInstance = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmfInstance.init(keyStore)
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmfInstance.trustManagers, null)
        return sslContext.socketFactory
    }
}

/* Stub hostname verifier is required to verify
the self-generated certificate of the LedLamp's Web Server*/
class StubHostnameVerifier : HostnameVerifier {
    @SuppressLint("BadHostnameVerifier")
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }
}