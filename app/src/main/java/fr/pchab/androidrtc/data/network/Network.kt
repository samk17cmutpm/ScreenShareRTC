package fr.pchab.androidrtc.data.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"
const val TIME_OUT = 1000000L

private val mGson = GsonBuilder().setDateFormat(DATE_FORMAT).create()

fun httpClient(debug: Boolean, context: Context?): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)

    val clientBuilder = OkHttpClient.Builder()

    clientBuilder
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)

    if (debug) {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    clientBuilder.addInterceptor(httpLoggingInterceptor)

    return clientBuilder.build()
}

fun retrofitClient(baseUrl: String, httpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(mGson))
        .build()

fun httpClientUpload(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS

    val clientBuilder = OkHttpClient.Builder()
    clientBuilder
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)

    val dispatcher = Dispatcher()
    dispatcher.maxRequests = 1
    clientBuilder.dispatcher(dispatcher)
    clientBuilder.addInterceptor(httpLoggingInterceptor)

    return clientBuilder.build()
}