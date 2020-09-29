package vn.udn.vku.webview

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

fun hasInternetAccess(): Boolean {
    return try {
        val urlc: HttpURLConnection = URL("http://clients3.google.com/generate_204")
            .openConnection() as HttpURLConnection
        urlc.setRequestProperty("User-Agent", "Android")
        urlc.setRequestProperty("Connection", "close")
        urlc.connectTimeout = 1500
        urlc.connect()
        return urlc.responseCode == 204 &&
                urlc.contentLength == 0
    } catch (e: IOException) {
        false
    }
}

class ConnectivityLiveData(
    private val connectivityManager: ConnectivityManager?,
) : LiveData<Boolean>() {
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            postValue(connected)
        }

        override fun onLost(network: Network) {
            postValue(false)
        }
    }

    private val networkRequest = NetworkRequest.Builder()

    override fun onActive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(callback)
        } else {
            connectivityManager?.registerNetworkCallback(networkRequest.build(), callback)
        }
    }

    override fun onInactive() {
        connectivityManager?.unregisterNetworkCallback(callback)
    }
}