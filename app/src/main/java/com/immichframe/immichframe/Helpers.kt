package com.immichframe.immichframe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Base64
import retrofit2.Call
import retrofit2.http.GET
import android.graphics.Rect
import androidx.core.graphics.scale


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

        val scaledLeftImage = scaleAspectFit(leftImage, targetHeight)
        val scaledRightImage = scaleAspectFit(rightImage, targetHeight)

        val width = scaledLeftImage.width + scaledRightImage.width + lineWidth
        val maxTextureSize = 4096 // might need to reduce

        val scaleFactor = if (width > maxTextureSize || targetHeight > maxTextureSize) {
            minOf(maxTextureSize.toFloat() / width, maxTextureSize.toFloat() / targetHeight)
        } else {
            1f
        }

        val resizedWidth = (width * scaleFactor).toInt()
        val resizedHeight = (targetHeight * scaleFactor).toInt()

        val result = Bitmap.createBitmap(resizedWidth, resizedHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val scaleLeft = (scaleFactor * scaledLeftImage.width).toInt()
        val scaleRight = (scaleFactor * scaledRightImage.width).toInt()
        val scaledLineWidth = (lineWidth * scaleFactor).toInt()

        val leftRect = Rect(0, 0, scaledLeftImage.width, scaledLeftImage.height)
        val leftDest = Rect(0, 0, scaleLeft, resizedHeight)
        canvas.drawBitmap(scaledLeftImage, leftRect, leftDest, paint)

        paint.color = lineColor
        canvas.drawRect(
            scaleLeft.toFloat(),
            0f,
            (scaleLeft + scaledLineWidth).toFloat(),
            resizedHeight.toFloat(),
            paint
        )

        val rightRect = Rect(0, 0, scaledRightImage.width, scaledRightImage.height)
        val rightDest = Rect(
            scaleLeft + scaledLineWidth,
            0,
            scaleLeft + scaledLineWidth + scaleRight,
            resizedHeight
        )
        canvas.drawBitmap(scaledRightImage, rightRect, rightDest, paint)

        if (!leftImage.isRecycled) leftImage.recycle()
        if (!rightImage.isRecycled) rightImage.recycle()
        if (!scaledLeftImage.isRecycled) scaledLeftImage.recycle()
        if (!scaledRightImage.isRecycled) scaledRightImage.recycle()

        return result
    }

    private fun scaleAspectFit(image: Bitmap, targetHeight: Int): Bitmap {
        val aspectRatio = image.width.toFloat() / image.height.toFloat()
        val targetWidth = (targetHeight * aspectRatio).toInt()

        if (image.width == targetWidth && image.height == targetHeight) {
            return image
        }

        val scaledBitmap = Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
        if (image != scaledBitmap && !image.isRecycled) {
            image.recycle()
        }

        return scaledBitmap
    }


    private fun calculateInSampleSize(
        origWidth: Int,
        origHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
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

    fun reduceBitmapQuality(bitmap: Bitmap, maxSize: Int = 1000): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate new dimensions while maintaining aspect ratio
        val scaleFactor = maxSize.toFloat() / width.coerceAtLeast(height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val resizedBitmap = bitmap.scale(newWidth, newHeight)

        return resizedBitmap
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