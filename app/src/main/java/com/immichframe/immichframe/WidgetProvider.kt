package com.immichframe.immichframe

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    fun updateBackground(context: Context, appWidgetId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("ImmichFramePrefs", Context.MODE_PRIVATE)
            val backgroundType =
                prefs.getString("widgetBackground$appWidgetId", "square") ?: "square"

            val views = RemoteViews(context.packageName, R.layout.widget_view)
            val backgroundRes = when (backgroundType) {
                "square" -> R.drawable.widget_background_square
                "round" -> R.drawable.widget_background_circle
                "squircle" -> R.drawable.widget_background_squircle
                else -> R.drawable.widget_background_square
            }
            views.setInt(R.id.widget_frame, "setBackgroundResource", backgroundRes)

            val intent = Intent(context, WidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.widgetImageView,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val savedUrl = prefs.getString("webview_url", "") ?: ""
                    val authSecret = prefs.getString("authSecret", "") ?: ""

                    if (savedUrl.isNotEmpty()) {
                        val retrofit = createRetrofit(savedUrl, authSecret)
                        val apiService = retrofit.create(Helpers.ApiService::class.java)

                        getNextImage(apiService) { imageResponse ->
                            imageResponse?.let {
                                val randomBitmap =
                                    Helpers.decodeBitmapFromBytes(it.randomImageBase64)

                                val reducedBitmap = reduceBitmapQuality(randomBitmap)

                                views.setImageViewBitmap(R.id.widgetImageView, reducedBitmap)

                                appWidgetManager.updateAppWidget(appWidgetId, views)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WidgetDebug", "Error updating widget: ${e.message}")
                }
            }
        }

        private fun getNextImage(
            apiService: Helpers.ApiService,
            callback: (Helpers.ImageResponse?) -> Unit
        ) {
            apiService.getImageData().enqueue(object : Callback<Helpers.ImageResponse> {
                override fun onResponse(
                    call: Call<Helpers.ImageResponse>,
                    response: Response<Helpers.ImageResponse>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body())
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<Helpers.ImageResponse>, t: Throwable) {
                    callback(null)
                }
            })
        }

        private fun reduceBitmapQuality(bitmap: Bitmap, quality: Int = 50): Bitmap {
            val outputStream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            val compressedByteArray = outputStream.toByteArray()

            val compressedBitmap =
                BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)

            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }

            return compressedBitmap
        }

        private fun createRetrofit(baseUrl: String, authSecret: String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()

                    val request = if (authSecret.isNotEmpty()) {
                        originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $authSecret")
                            .build()
                    } else {
                        originalRequest
                    }

                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}
