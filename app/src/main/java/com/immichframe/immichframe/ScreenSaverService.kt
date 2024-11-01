package com.immichframe.immichframe

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.service.dreams.DreamService
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class ScreenSaverService : DreamService() {
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen=true
        isInteractive=false
        setContentView(R.layout.screen_saver_view)

        webView = findViewById(R.id.screensaver_webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        loadSavedUrl()
        acquireWakeLock()
    }

    private fun loadSavedUrl() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        if (savedUrl != null) {
            webView.loadUrl(savedUrl)
        }
    }
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        releaseWakeLock()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseWakeLock()
    }
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,"ImmichFrame::ScreenSaverWakeLock")
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