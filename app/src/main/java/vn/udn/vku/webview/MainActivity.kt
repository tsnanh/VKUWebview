package vn.udn.vku.webview

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.udn.vku.webview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val dialog by lazy {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Không có kết nối Internet")
            setMessage("Internet đã bị ngắt hoặc không có sẵn. Bạn vui lòng kiểm tra lại kết nối!")
            setCancelable(false)
        }.create()
    }

    private val dialogExit by lazy {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Không có kết nối Internet")
            setMessage("Internet đã bị ngắt hoặc không có sẵn. Bạn vui lòng kiểm tra lại kết nối!")
            setCancelable(false)
            setPositiveButton("Thoát") { _, _ ->
                finish()
            }
        }.create()
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (dialog.isShowing) {
                    finish()
                }
            }
            true
        }

        val cm = getSystemService<ConnectivityManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (!hasInternetAccess()) {
                    withContext(Dispatchers.Main) {
                        dialogExit.show()
                    }
                }
            }
        } else {
            val activeNetwork: NetworkInfo? = cm?.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
            if (!isConnected) {
                dialogExit.show()
            }
        }

        with(binding.webView) {
            settings.javaScriptEnabled = true
            loadUrl("https://www.google.com/search?q=vku&oq=vku&aqs=chrome.0.69i59j0j69i60l6.1724j0j4&client=ubuntu&sourceid=chrome&ie=UTF-8")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let {
                        if (url.contains("vku")) {
                            val dialog = PopupFragment.newInstance(url)
                            dialog.show(supportFragmentManager, "vku")
                        } else {
                            view?.loadUrl(url)
                        }
                    }
                    return true
                }
            }
        }

        viewModel.connectivityLiveData.observe(this) { available ->
            println(available)
            available?.let {
                if (available) {
                    binding.webView.reload()
                    dialog.hide()
                    dialogExit.hide()
                } else {
                    dialog.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else
            finish()
    }
}