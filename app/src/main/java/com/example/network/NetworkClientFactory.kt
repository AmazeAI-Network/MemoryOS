package com.example.network

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Enterprise Network Client Factory.
 * Configures disk caching, offline resilience interceptors, and strict timeouts
 * to ensure smooth performance under slow or intermittent connectivity.
 */
object NetworkClientFactory {

  private const val CACHE_SIZE = 20L * 1024 * 1024 // 20 MB disk cache
  private const val CONNECT_TIMEOUT_SECONDS = 15L
  private const val READ_TIMEOUT_SECONDS = 20L
  private const val WRITE_TIMEOUT_SECONDS = 20L

  fun createOkHttpClient(
    context: Context,
    connectivityObserver: NetworkConnectivityObserver? = null
  ): OkHttpClient {
    val cacheDir = File(context.cacheDir, "http_cache")
    val cache = Cache(cacheDir, CACHE_SIZE)

    val observer = connectivityObserver ?: NetworkConnectivityObserver(context)

    return OkHttpClient.Builder()
      .cache(cache)
      .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .addInterceptor(createOfflineCacheInterceptor(observer))
      .addNetworkInterceptor(createOnlineCacheInterceptor())
      .addInterceptor(createSecurityHeadersInterceptor())
      .retryOnConnectionFailure(true)
      .build()
  }

  /**
   * Rewrites response headers to instruct OkHttp to cache responses for 60 seconds when online.
   */
  private fun createOnlineCacheInterceptor(): Interceptor {
    return Interceptor { chain ->
      val response = chain.proceed(chain.request())
      val cacheControl = CacheControl.Builder()
        .maxAge(60, TimeUnit.SECONDS)
        .build()

      response.newBuilder()
        .removeHeader("Pragma")
        .removeHeader("Cache-Control")
        .header("Cache-Control", cacheControl.toString())
        .build()
    }
  }

  /**
   * Forces OkHttp to use stale cached responses up to 7 days if the device is currently offline.
   */
  private fun createOfflineCacheInterceptor(connectivity: NetworkConnectivityObserver): Interceptor {
    return Interceptor { chain ->
      var request = chain.request()

      if (!connectivity.isCurrentlyConnected()) {
        val cacheControl = CacheControl.Builder()
          .onlyIfCached()
          .maxStale(7, TimeUnit.DAYS)
          .build()

        request = request.newBuilder()
          .cacheControl(cacheControl)
          .build()
      }

      chain.proceed(request)
    }
  }

  /**
   * Injects security headers into every outgoing request.
   */
  private fun createSecurityHeadersInterceptor(): Interceptor {
    return Interceptor { chain ->
      val original = chain.request()
      val request = original.newBuilder()
        .header("X-Content-Type-Options", "nosniff")
        .header("X-App-Platform", "Android-MemoryOS")
        .build()
      chain.proceed(request)
    }
  }
}
