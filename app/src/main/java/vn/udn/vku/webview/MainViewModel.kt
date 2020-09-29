package vn.udn.vku.webview

import android.app.Application
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val connectivityLiveData =
        ConnectivityLiveData(getApplication<Application>().applicationContext.getSystemService())
}