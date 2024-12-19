package com.immichframe.immichframe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.dreams.DreamService
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Base64
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.immichframe.immichframe.helpers.TextSizeHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScreenSaverService : DreamService() {
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var webView: WebView
    private lateinit var imageView: ImageView
    private lateinit var txtPhotoInfo: TextView
    private lateinit var txtDateTime: TextView
    private lateinit var serverSettings: ServerSettings
    private var retrofit: Retrofit? = null
    private lateinit var apiService: ApiService
    private var isTimerRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var useWebView = true

    data class ImageResponse(
        val randomImageBase64: String,
        val thumbHashImageBase64: String,
        val photoDate: String,
        val imageLocation: String
    )

    data class ServerSettings(
        val imageStretch: String,
        val margin: String,
        val interval: Int,
        val transitionDuration: Int,
        val downloadImages: Boolean,
        val renewImagesDuration: Int,
        val showClock: Boolean,
        val clockFormat: String,
        val showPhotoDate: Boolean,
        val photoDateFormat: String,
        val showImageDesc: Boolean,
        val showPeopleDesc: Boolean,
        val showImageLocation: Boolean,
        val imageLocationFormat: String,
        val primaryColor: String?,
        val secondaryColor: String,
        val style: String,
        val baseFontSize: String?,
        val showWeatherDescription: Boolean,
        val unattendedMode: Boolean,
        val imageZoom: Boolean,
        val layout: String,
        val language: String
    )

    interface ApiService {
        @GET("api/Asset/RandomImageAndInfo")
        fun getImageData(): Call<ImageResponse>

        @GET("api/Config")
        fun getServerSettings(): Call<ServerSettings>
    }

    override fun onDreamingStarted() {
        super.onAttachedToWindow()
        isFullscreen = true
        isInteractive = false
        setContentView(R.layout.screen_saver_view)
        webView = findViewById(R.id.webView)
        imageView = findViewById(R.id.imageView)
        txtPhotoInfo = findViewById(R.id.txtPhotoInfo)
        txtDateTime = findViewById(R.id.txtDateTime)

        acquireWakeLock()
        loadSettings()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        releaseWakeLock()
        handler.removeCallbacksAndMessages(null)
    }

    private fun getNextImage() {
        apiService.getImageData().enqueue(object :
            Callback<ImageResponse> {
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (response.isSuccessful) {
                    val imageResponse = response.body()
                    if (imageResponse != null) {
                        val decodedRandomImage =
                            Base64.decode(imageResponse.randomImageBase64, Base64.DEFAULT)
                        val decodedThumbHash =
                            Base64.decode(imageResponse.thumbHashImageBase64, Base64.DEFAULT)

                        val randomBitmap = BitmapFactory.decodeByteArray(
                            decodedRandomImage,
                            0,
                            decodedRandomImage.size
                        )
                        val thumbHashBitmap = BitmapFactory.decodeByteArray(
                            decodedThumbHash,
                            0,
                            decodedThumbHash.size
                        )

                        imageView.setImageBitmap(randomBitmap)
                        imageView.alpha = 0f
                        imageView.setImageBitmap(randomBitmap)
                        imageView.background = BitmapDrawable(resources, thumbHashBitmap)
                        imageView.animate().alpha(1f)
                            .setDuration((serverSettings.transitionDuration * 1000).toLong())
                            .start()

                        if (serverSettings.showPhotoDate || serverSettings.showImageLocation) {
                            val photoInfo = buildString {
                                if (serverSettings.showPhotoDate && imageResponse.photoDate.isNotEmpty()) {
                                    append(imageResponse.photoDate)
                                }
                                if (serverSettings.showImageLocation && imageResponse.imageLocation.isNotEmpty()) {
                                    if (isNotEmpty()) append("\n")
                                    append(imageResponse.imageLocation)
                                }
                            }
                            txtPhotoInfo.text = photoInfo
                        }

                        if (serverSettings.showClock) {
                            val currentDateTime = Calendar.getInstance().time
                            val dateFormatter = SimpleDateFormat(
                                serverSettings.photoDateFormat,
                                Locale.getDefault()
                            )
                            val timeFormatter =
                                SimpleDateFormat(serverSettings.clockFormat, Locale.getDefault())
                            val formattedDate = dateFormatter.format(currentDateTime)
                            val formattedTime = timeFormatter.format(currentDateTime)
                            val dt = "$formattedDate\n$formattedTime"
                            val spannableString = SpannableString(dt)
                            spannableString.setSpan(
                                RelativeSizeSpan(2f),
                                formattedDate.length + 1,
                                dt.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            txtDateTime.text = spannableString
                        }
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to load image (HTTP ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Failed to load image: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getServerSettings(
        onSuccess: (ServerSettings) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        apiService.getServerSettings().enqueue(object :
            Callback<ServerSettings> {
            override fun onResponse(
                call: Call<ServerSettings>,
                response: Response<ServerSettings>
            ) {
                if (response.isSuccessful) {
                    val serverSettingsResponse = response.body()
                    if (serverSettingsResponse != null) {
                        onSuccess(serverSettingsResponse)
                    } else {
                        onFailure(Exception("Empty response body"))
                    }
                } else {
                    onFailure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<ServerSettings>, t: Throwable) {
                onFailure(t)
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadSettings() {
        val sharedPreferences = getSharedPreferences("ImmichFramePrefs", MODE_PRIVATE)
        var savedUrl = sharedPreferences.getString("webview_url", getString(R.string.webview_url))
        useWebView = sharedPreferences.getBoolean("useWebView", true)
        if (!savedUrl?.endsWith("/")!!) {
            savedUrl += "/"
        }
        if (useWebView) {
            handler.removeCallbacksAndMessages(null)
            webView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            txtPhotoInfo.visibility = View.GONE
            txtDateTime.visibility = View.GONE
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
            webView.settings.javaScriptEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.settings.domStorageEnabled = true
            webView.loadUrl(savedUrl)
        } else {
            webView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            retrofit = createRetrofit(savedUrl)
            apiService = retrofit!!.create(ApiService::class.java)
            getServerSettings(
                onSuccess = { settings ->
                    serverSettings = settings
                    onSettingsLoaded()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this,
                        "Failed to load server settings: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun onSettingsLoaded() {
        if (serverSettings.showPhotoDate || serverSettings.showImageLocation) {
            txtPhotoInfo.visibility = View.VISIBLE
            txtPhotoInfo.textSize =
                TextSizeHelper.cssFontSizeToSp(serverSettings.baseFontSize, this)
            if (serverSettings.primaryColor != null) {
                txtPhotoInfo.setTextColor(
                    runCatching { Color.parseColor(serverSettings.primaryColor) }.getOrDefault(Color.WHITE)
                )
            } else {
                txtPhotoInfo.setTextColor(Color.WHITE)
            }
        } else {
            txtPhotoInfo.visibility = View.GONE
        }
        if (serverSettings.showClock) {
            txtDateTime.visibility = View.VISIBLE
            txtDateTime.textSize = TextSizeHelper.cssFontSizeToSp(serverSettings.baseFontSize, this)
            if (serverSettings.primaryColor != null) {
                txtDateTime.setTextColor(
                    runCatching { Color.parseColor(serverSettings.primaryColor) }.getOrDefault(Color.WHITE)
                )
            } else {
                txtDateTime.setTextColor(Color.WHITE)
            }
        } else {
            txtDateTime.visibility = View.GONE
        }

        getNextImage()

        if (!isTimerRunning) {
            isTimerRunning = true
            handler.postDelayed(object : Runnable {
                override fun run() {
                    getNextImage()
                    handler.postDelayed(this, (serverSettings.interval * 1000).toLong())
                }
            }, (serverSettings.interval * 1000).toLong())
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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