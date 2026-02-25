package com.a2ui.renderer.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Network status detector
 * Monitors network connectivity and provides flow-based updates
 */
class NetworkStatus(private val context: Context) {
    
    private val _isOnline = MutableStateFlow(checkNetworkStatus())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _networkType = MutableStateFlow(getNetworkType())
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    /**
     * Network types
     */
    enum class NetworkType {
        NONE,
        MOBILE,
        WIFI,
        ETHERNET,
        UNKNOWN
    }
    
    /**
     * Initialize network monitoring
     */
    fun startMonitoring() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                _networkType.value = getNetworkType()
            }
            
            override fun onLost(network: Network) {
                _isOnline.value = checkNetworkStatus()
                _networkType.value = getNetworkType()
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                _isOnline.value = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                _networkType.value = getNetworkType()
            }
        }
        
        networkCallback?.let {
            connectivityManager?.registerNetworkCallback(networkRequest, it)
        }
    }
    
    /**
     * Stop network monitoring
     */
    fun stopMonitoring() {
        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }
        networkCallback = null
        connectivityManager = null
    }
    
    /**
     * Check current network status
     */
    fun checkNetworkStatus(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get current network type
     */
    fun getNetworkType(): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.UNKNOWN
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    /**
     * Flow of network status changes
     */
    fun networkStatusFlow(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Send initial status
        trySend(checkNetworkStatus())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    
    /**
     * Check if network is metered (e.g., mobile data)
     */
    fun isMetered(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isActiveNetworkMetered()
    }
    
    /**
     * Get estimated bandwidth in bits per second
     */
    fun getBandwidthEstimate(): Long {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return 0
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return 0
        
        return capabilities.linkDownstreamBandwidthKbps * 1000L
    }
}
