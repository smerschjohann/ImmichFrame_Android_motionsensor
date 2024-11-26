package com.immichframe.immichframe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.service.dreams.DreamService
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class ScreenSaverService : DreamService() {
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onDreamingStarted() {
        super.onAttachedToWindow()
        isFullscreen = true
        isInteractive = false
        setContentView(R.layout.screen_saver_view)

        webView = findViewById(R.id.screensaver_webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url
                if (url != null) {
                    // Open the URL in the default browser
                    val intent = Intent(Intent.ACTION_VIEW, url)
                    startActivity(intent)
                    return true
                }
                return false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                view?.reload()
            }
        }
        loadSavedUrl()
        acquireWakeLock()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        releaseWakeLock()
    }

    private fun loadSavedUrl() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        if (savedUrl != null) {
            webView.loadUrl(savedUrl)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "ImmichFrame::ScreenSaverWakeLock"
            )
            wakeLock?.acquire(120 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
}