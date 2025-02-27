package com.immichframe.immichframe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Base64
import retrofit2.Call
import retrofit2.http.GET

object Helpers {
    fun textSizeMultiplier(context: Context, currentSizeSp: Float, multiplier: Float): Float {
        val resources = context.resources
        val fontScale = resources.configuration.fontScale
        val density = resources.displayMetrics.density
        val currentSizePx = currentSizeSp * density * fontScale
        val newSizePx = currentSizePx * multiplier

        return newSizePx / (density * fontScale)
    }

    fun cssFontSizeToSp(cssSize: String?, context: Context, baseFontSizePx: Float = 16f): Float {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val fontScale = resources.configuration.fontScale
        val density = displayMetrics.density

        // Handle null cssSize
        val effectiveCssSize = cssSize ?: "medium"

        return when {
            effectiveCssSize.equals("xx-small", ignoreCase = true) -> 8f * fontScale
            effectiveCssSize.equals("x-small", ignoreCase = true) -> 10f * fontScale
            effectiveCssSize.equals("small", ignoreCase = true) -> 12f * fontScale
            effectiveCssSize.equals("medium", ignoreCase = true) -> 16f * fontScale
            effectiveCssSize.equals("large", ignoreCase = true) -> 20f * fontScale
            effectiveCssSize.equals("x-large", ignoreCase = true) -> 24f * fontScale
            effectiveCssSize.equals("xx-large", ignoreCase = true) -> 32f * fontScale

            effectiveCssSize.endsWith("px", ignoreCase = true) -> {
                val px = effectiveCssSize.removeSuffix("px").toFloatOrNull() ?: baseFontSizePx
                px / (density * fontScale)
            }

            effectiveCssSize.endsWith("pt", ignoreCase = true) -> {
                val pt = effectiveCssSize.removeSuffix("pt").toFloatOrNull() ?: baseFontSizePx
                val px = pt * (density * 160f / 72f)
                px / (density * fontScale)
            }

            effectiveCssSize.endsWith("em", ignoreCase = true) -> {
                val em = effectiveCssSize.removeSuffix("em").toFloatOrNull() ?: 1f
                val px = em * baseFontSizePx
                px / (density * fontScale)
            }

            else -> 16f * fontScale
        }
    }

    fun mergeImages(leftImage: Bitmap, rightImage: Bitmap, lineColor: Int): Bitmap {
        val lineWidth = 10
        val targetHeight = maxOf(leftImage.height, rightImage.height)

        // Scale images efficiently
        val scaledLeftImage = scaleAspectFit(leftImage, targetHeight)
        val scaledRightImage = scaleAspectFit(rightImage, targetHeight)

        val width = scaledLeftImage.width + scaledRightImage.width + lineWidth

        // Try to allocate a smaller Bitmap if memory is tight
        val result = try {
            Bitmap.createBitmap(width, targetHeight, Bitmap.Config.RGB_565)
        } catch (e: OutOfMemoryError) {
            System.gc()
            Bitmap.createBitmap(width / 2, targetHeight / 2, Bitmap.Config.RGB_565) // Fallback with smaller bitmap
        }

        val canvas = Canvas(result)

        // Draw left image
        canvas.drawBitmap(scaledLeftImage, 0f, 0f, null)

        // Draw separating line
        val paint = Paint().apply {
            color = lineColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            scaledLeftImage.width.toFloat(),
            0f,
            (scaledLeftImage.width + lineWidth).toFloat(),
            targetHeight.toFloat(),
            paint
        )

        // Draw right image
        canvas.drawBitmap(scaledRightImage, (scaledLeftImage.width + lineWidth).toFloat(), 0f, null)

        // Recycle old bitmaps ASAP
        leftImage.recycle()
        rightImage.recycle()
        scaledLeftImage.recycle()
        scaledRightImage.recycle()

        return result
    }

    private fun scaleAspectFit(image: Bitmap, targetHeight: Int): Bitmap {
        val aspectRatio = image.width.toFloat() / image.height.toFloat()
        val targetWidth = (targetHeight * aspectRatio).toInt()

        BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565 // Reduce memory usage
            inSampleSize = calculateInSampleSize(image.width, image.height, targetWidth, targetHeight)
        }

        return Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
    }

    private fun calculateInSampleSize(origWidth: Int, origHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (origHeight > reqHeight || origWidth > reqWidth) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }



    fun decodeBitmapFromBytes(data: String): Bitmap {
        val decodedImage = Base64.decode(data, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
        return bmp
    }
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
        val transitionDuration: Double,
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

    data class Weather(
        val location: String,
        val temperature: Double,
        val unit: String,
        val temperatureUnit: String,
        val description: String,
        val iconId: String
    )

    interface ApiService {
        @GET("api/Asset/RandomImageAndInfo")
        fun getImageData(): Call<ImageResponse>

        @GET("api/Config")
        fun getServerSettings(): Call<ServerSettings>

        @GET("api/Weather")
        fun getWeather(): Call<Weather>
    }


}