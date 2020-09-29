package vn.udn.vku.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.addCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vn.udn.vku.webview.databinding.FragmentPopupBinding
import java.util.*

class PopupFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentPopupBinding

    companion object {
        fun newInstance(url: String) = PopupFragment().apply {
            arguments = Bundle().apply {
                putString("url", url)
            }
        }
    }

    private val stack = Stack<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireView().viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as? BottomSheetDialog
            val bottomSheet =
                dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.top
            val height = (requireContext().resources.displayMetrics.heightPixels * 0.95).toInt()
            println(height)
            bottomSheet?.let {
                BottomSheetBehavior.from<FrameLayout>(bottomSheet).apply {
                    isDraggable = false
                    state = BottomSheetBehavior.STATE_COLLAPSED
                    setPeekHeight(height, true)
                    this.expandedOffset = 1
                }
            }
        }
    }

    override fun getTheme() = R.style.CustomBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val url = requireArguments().getString("url")
        with(binding.toolbar) {
            setNavigationOnClickListener {
                dismiss()
            }
        }
        binding.url.text = url
        stack.add(url)
        binding.back.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
                stack.pop()
                binding.url.text = stack.peek()
            }
        }
        with(binding.webView) {
            settings.javaScriptEnabled = true
            if (url != null) {
                loadUrl(url)
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let(::loadUrl)
                    binding.url.text = url
                    stack.add(url)
                    return true
                }
            }
        }
    }
}