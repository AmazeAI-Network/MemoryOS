package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkStatus {
  AVAILABLE,
  LOSING,
  LOST,
  UNAVAILABLE
}

/**
 * Enterprise global network state observer.
 * Listens for system network transitions via Android ConnectivityManager.NetworkCallback.
 */
class NetworkConnectivityObserver(context: Context) {

  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val _networkStatus = MutableStateFlow(getCurrentStatus())
  val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

  private val _isConnected = MutableStateFlow(isCurrentlyConnected())
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      _networkStatus.value = NetworkStatus.AVAILABLE
      _isConnected.value = true
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
      _networkStatus.value = NetworkStatus.LOSING
    }

    override fun onLost(network: Network) {
      _networkStatus.value = NetworkStatus.LOST
      _isConnected.value = isCurrentlyConnected()
    }

    override fun onUnavailable() {
      _networkStatus.value = NetworkStatus.UNAVAILABLE
      _isConnected.value = false
    }
  }

  init {
    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    try {
      connectivityManager.registerNetworkCallback(request, networkCallback)
    } catch (_: Exception) {
      // Graceful fallback for restricted Android environments
    }
  }

  fun isCurrentlyConnected(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
  }

  private fun getCurrentStatus(): NetworkStatus {
    return if (isCurrentlyConnected()) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE
  }

  fun refresh() {
    val connected = isCurrentlyConnected()
    _isConnected.value = connected
    _networkStatus.value = if (connected) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE
  }

  /**
   * Memory Leak Prevention: Safely unregisters the system network callback
   * when the host Activity / ViewModel / Composable is destroyed.
   */
  fun unregister() {
    try {
      connectivityManager.unregisterNetworkCallback(networkCallback)
    } catch (_: Exception) {
      // Graceful fallback if already unregistered or system service unavailable
    }
  }
}
