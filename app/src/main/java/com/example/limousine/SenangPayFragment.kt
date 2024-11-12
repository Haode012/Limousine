package com.example.limousine

// SenangPayFragment.kt
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import java.net.URLEncoder

class SenangPayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_senangpay, container, false)

        val webView: WebView = rootView.findViewById(R.id.webView)
        val progressBar: ProgressBar = rootView.findViewById(R.id.progressBar4)

        // Enable JavaScript for SenangPay page
        webView.settings.javaScriptEnabled = true

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                // Update progress bar based on the loading progress
                progressBar.progress = newProgress

                if (newProgress == 100) {
                    // Loading complete, hide the progress bar and show the WebView
                    progressBar.visibility = View.INVISIBLE
                    webView.visibility = View.VISIBLE
                }
            }
        }

        // Load SenangPay's payment page
     val senangPayUrl = "https://app.senangpay.my/payment/601168612492232" // Replace with the actual URL
     webView.loadUrl(senangPayUrl)

        return rootView
    }
}